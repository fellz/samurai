(ns frontend.views
  (:require
    [frontend.core as core]
    [reagent.core :as reagent :refer [atom]]))

(def pdata (atom {:id 0
                  :fio "",
                  :adress "",
                  :birthday "",
                  :police_id 0,
                  :sex ""}))


;; Views

(defn update-patient-view []
  (let [{:keys [fio adress sex birthday police_id]} @pdata ;; data is list (data)
        ]
    [:div
     [:div {:style {:color "red"}} "Edit info here"]
     [:div
      [:label "FIO"]
      [:input {:type "text"
               :value fio
               :on-change #(swap! pdata assoc :fio (-> % .-target .-value))}]]
     [:div
      [:label "Adress"]
      [:input {:type "text"
               :value adress
               :on-change #(swap! pdata assoc :adress (-> % .-target .-value))}]]
     [:div
      [:label "Birthday"]
      [:input {:type "text"
               :value birthday
               :on-change #(swap! pdata assoc :birthday (-> % .-target .-value))}]]
     [:div
      [:label "Sex"]
      [:input {:type "text"
               :value sex
               :on-change #(swap! pdata assoc :sex (-> % .-target .-value))}]]
     [:div
      [:label "Police id"]
      [:input {:type "text"
               :value police_id
               :on-change #(swap! pdata assoc :police_id (-> % .-target .-value))}]]
     [:hr]
     [:button {:on-click #(core/update-patient @pdata)} "Submit"]
     [:button {:on-click #(core/delete-patient (:id @pdata))} "Delete"]]))

(defn create-patient-view []
  [:div
   [:div
    [:label "FIO"]
    [:input {:type "text"
             :on-change #(swap! pdata assoc :fio (-> % .-target .-value))}]]
   [:div
    [:label "Adress"]
    [:input {:type "text"
             :on-change #(swap! pdata assoc :adress (-> % .-target .-value))}]]
   [:div
    [:label "Birthday"]
    [:input {:type "text"
             :on-change #(swap! pdata assoc :birthday (-> % .-target .-value))}]]
   [:div
    [:label "Sex"]
    [:input {:type "text"
             :on-change #(swap! pdata assoc :sex (-> % .-target .-value))}]]
   [:div
    [:label "Police id"]
    [:input {:type "text"
             :on-change #(swap! pdata assoc :police_id (-> % .-target .-value))}]]

   [:button {:on-click #(core/create-patient (dissoc @pdata :id))} "Submit"]])

(defn patient-view []
  (let [{:keys [fio adress sex birthday police_id]} @pdata]
    [:div
     [:div [:span "FIO: "] [:span {:style {:font-weight "bold"}} fio]]
     [:div [:span "Adress: "] [:span {:style {:font-weight "bold"}} adress]]
     [:div [:span "Birthday: "] [:span {:style {:font-weight "bold"}} birthday]]
     [:div [:span "Sex: "] [:span {:style {:font-weight "bold"}} sex]]
     [:div [:span "Police id: "] [:span {:style {:font-weight "bold"}} police_id]]]))

(defn all-patients-view []
  [:ul
   (for [patient @core/patients-data]
     ^{:key patient} [:li
                      [:a {:href (path-for
                                  :patient
                                    {:patient-id (:id patient)})} "Patient FIO: " (:fio patient)]])])


