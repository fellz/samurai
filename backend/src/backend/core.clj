(ns backend.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.java.jdbc :as j]
            [clojure.data.json :as json]
            [clj-time.jdbc]
            [clj-time.format :as f]
            [clj-time.local :as l])
  (:gen-class))

(def app-db {:dbtype "postgresql"
               :dbname "samurai"
               :host "localhost"
               :user "postgres"
               :password "postgres"
               :ssl true
               :sslfactory "org.postgresql.ssl.NonValidatingFactory"})

;; Helper functions
(defn as-date-string [date]
  (f/unparse (f/formatter "dd MMM YYYY") date))

(defn date-aware-value-writer [key value] 
  (if (= key :birthday) (as-date-string value) value))

;; Model operations. Model - Patient

(defn get-all-patients-db []
  (let [all-patients (j/query app-db ["select * from patients AS result"])]
    ;;(println all-patients)
    all-patients))

(defn get-patient-db [id]
  (j/query app-db
           ["select * from patients where id = ? " (Integer/parseInt id)]))

(defn create-patient-db [patient]
  (if (not (empty? patient))
    (let [patient-w-date (assoc patient :birthday (l/local-now) :police_id (Integer/parseInt (:police_id patient)))]
      (try
        (j/insert! app-db :patients patient-w-date )
      (catch Exception e
        (println "Exception message: "  e ))))
    {:message "Error"}))


(defn update-patient-db [patient]
  (let [id (Integer/parseInt (:id patient))
        patient-wo-id (dissoc patient :id)
        patient-w-date (assoc patient-wo-id
                              :birthday (l/local-now)
                              :police_id (:police_id patient))]
  (try
    (j/update! app-db :patients patient-w-date ["id = ?" id])
    (catch Exception e
      (println "Exception message: " (.getNextException e))))))



(defn delete-patient-db [id]
  (j/delete! app-db :patients ["id = ?" (Integer/parseInt id)]))


;; Controllers

;; Display all patients from db
(defn patients [req]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str (get-all-patients-db) :value-fn date-aware-value-writer)})

(defn patient [id]
  (let [ patient (get-patient-db id)]
  {:status  200
   :headers {"content-type" "application/json"}
   :body    (json/write-str patient )}))

(defn create-patient [req]
  (let [{:keys [params]} req
        patients (first (create-patient-db params))]

      {:status 200
        :headers {"Content-Type" "application/json"}
       :body (json/write-str (assoc patients :birthday (.toString (:birthday patients))))}))

(defn update-patient [req]
  (let [{:keys [params]} req]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/write-str (update-patient-db params))}))

(defn delete-patient [id]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str (delete-patient-db id))})

; Our main routes
(defroutes app-routes
  (GET "/" [] patients)
  (GET "/:id" [id] (patient id))
  (ANY "/" [] (-> create-patient wrap-keyword-params wrap-json-params ))
  (PUT "/:id" [] (-> update-patient wrap-keyword-params wrap-json-params ))
  (DELETE "/:id" [id] (delete-patient id))

  (route/not-found "Error, page not found!"))
(def app-routes-cors
  (wrap-cors app-routes :access-control-allow-origin [#"http://localhost:3449"]
                        :access-control-allow-methods [:get :put :post :delete]))
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes-cors api-defaults) {:port port})
    ; Run the server without ring defaults
    ;(server/run-server #'app-routes {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
