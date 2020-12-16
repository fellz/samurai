(ns frontend.core
  (:require
   [frontend.view as fview]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [reitit.frontend.easy :as rfe]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [cljs.core.async :refer [<!]]
   [cljs-http.client :as http]
   [ajax.core :as ajax])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;;  :id 11
;;  :fio "Ivan Petrovich Bobrov"
;;  :adress "Moscow, Tverskaya str. 34-3"
;;  :birthday "11-04-1976"
;;  :police_id 3235235
;;  :sex "male"

(def patients-data (atom nil))
(def response-data (atom nil))
;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/create-patient" :create]
    ["/patients"
     ["/:patient-id" :patient]]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))


;; -- AJAX requests


(defn errors-handler []
  (js/setTimeout #(reset! response-data nil) 3000)
  [:div
   [:h3 {:style {:color "red"}} (:message @response-data)]])

(defn err-handler [resp]
  (reset! response-data {:message (str "Ошибка: " (:status-text resp))}))

;; Controllers


(defn get-all-patients []
  (ajax/GET "http://localhost:3000/"
   {:handler (fn [[ok response]] (if ok
                                   (reset! patients-data response)
                                   (err-handler response)))
    :error-handler err-handler
    :format (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})}))

(defn create-patient [data]
  (ajax/POST "http://127.0.0.1:3000/"
    :params data
    :handler (fn [resp]
               (reset! response-data {:message (str "Успешно!")})
               (accountant/navigate! (path-for :index)))
    :error-handler err-handler
    :format (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})))

(defn update-patient [data]
  (let [id (:id data)
        url (str "http://localhost:3000/" id)
        data-wo-id (dissoc data :id)]
    (ajax/PUT url
      {:params data-wo-id
       :handler (fn [resp] (reset! response-data {:message (str "Успешно!")}))
       :error-handler err-handler
       :format (ajax/json-request-format)
       :response-format :json
       :keywords? true})))

(defn delete-patient [id]
  (let [url (str "http://localhost:3000/" id)]
    (ajax/DELETE url
     {:handler (fn [resp]
                 (reset! response-data {:message (str "Успешно!")})
                 (accountant/navigate! (path-for :index)))
      :error-handler err-handler
      :format (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})})))


;; Pages


(defn home-page []
  (get-all-patients) ;; load data on the first run
  (fn []
    [:span.main
     [:h2 "Welcome to Johns Hopkins Hospital"]
     [all-patients-view]]))

(defn create-patient-page []
  (fn []
    [:div
     [create-patient-view]])) ;; open on new page

(defn patient-page []
  "Take id from session and get patient from patients-data atom and update pdata atom with it"
  (let [routing-data (session/get :route)
        patient (get-in routing-data [:route-params :patient-id]) ;; patient string "2"
        patient-num (js/parseInt patient 10)
        patient-data (first (filter #(= (:id %) patient-num)  @patients-data))]
    (reset! fview/pdata patient-data)
    (fn []
      [:div
       [:div.main
        [patient-view]
        [:hr]
        [update-patient-view]
        [:p [:a {:href (path-for :index)} "Back"]]]])))


;; -------------------------
;; Translate routes -> page components


(defn page-for [route]
  (case route
    :index #'home-page
    :patient #'patient-page
    :create #'create-patient-page))


;; -------------------------
;; Page mounting component


(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p
         [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :create)} "Create patient"]]]
       [errors-handler]
       [page]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
