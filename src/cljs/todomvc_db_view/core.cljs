(ns todomvc-db-view.core
  (:require [reagent.core :as r]
            [todomvc-db-view.state.core :as state]
            [todomvc-db-view.db-view.get :as db-view]
            [todomvc-db-view.command.send :as command])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce todos (r/atom (sorted-map)))

(defonce counter (r/atom 0))

(defn add-todo
  [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :title text :done false})))

(defn toggle
  [id]
  (swap! todos update-in [id :done] not))

(defn save
  [id title]
  (swap! todos assoc-in [id :title] title))

(defn delete
  [id]
  (swap! todos dissoc id))

(defn mmap [m f a] (->> m (f a) (into (empty m))))
(defn complete-all [v] (swap! todos mmap map #(assoc-in % [1 :done] v)))
(defn clear-done [] (swap! todos mmap remove #(get-in % [1 :done])))

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val (r/atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (if-not (empty? v) (on-save v))
                (stop))]
    (fn [{:keys [id class placeholder]}]
      [:input {:type "text" :value @val
               :id id :class class :placeholder placeholder
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(def todo-list-cursor
  (state/cursor [:db-view/value
                 :todo/list]))

(def todo-list-params-cursor
  (state/cursor [:db-view/params
                 :todo/list]))

(def todo-edit (with-meta todo-input
                 {:component-did-mount #(.focus (r/dom-node %))}))

(defn select-filter!
  "Selects the filter (`:all`, `:active` or `:completed`) for the todo
   list items and refreshes the db-view."
  [filter-name]
  (swap! todo-list-params-cursor
         assoc
         :todo/filter filter-name)
  (db-view/refresh!))

(defn todo-stats []
  (let [selected (:todo/filter @todo-list-params-cursor
                               :all)
        props-for (fn [name]
                    {:class (if (= name selected) "selected")
                     :on-click (fn [_e]
                                 (select-filter! name))})]
    [:div
     (let [active-count (:todo/active-count @todo-list-cursor)]
       [:span#todo-count
        [:strong active-count] " " (case active-count
                                     1
                                     "item"
                                     "items") " left"])
     [:ul#filters
      [:li [:a (props-for :all) "All"]]
      [:li [:a (props-for :active) "Active"]]
      [:li [:a (props-for :completed) "Completed"]]]
     #_(when (pos? done)
         [:button#clear-completed {:on-click clear-done}
          "Clear completed " done])]))

(defn send-command!
  "Sends an encrypted command map to the server."
  [command]
  (go
    (<! (command/send! command))))

(defn todo-item []
  (let [editing (r/atom false)]
    (fn [{:keys [db/id todo/done todo/title todo/done! todo/active! todo/delete!]}]
      [:li {:class (str (if done "completed ")
                        (if @editing "editing"))}
       [:div.view
        [:input.toggle {:type "checkbox"
                        :checked done
                        :on-change (fn [_e]
                                     (if done!
                                       (send-command! done!)
                                       (send-command! active!)))}]
        [:label {:on-double-click #(reset! editing true)} title]
        [:button.destroy {:on-click
                          (fn []
                            (send-command! delete!))}]]
       (when @editing
         [todo-edit {:class "edit" :title title
                     :on-save #(save id %)
                     :on-stop #(reset! editing false)}])])))

(defn todo-app [props]
  (let [filt (r/atom :all)]
    (fn []
      (let [items (:todo/list-items @todo-list-cursor)
            ;; done (->> items (filter :done) count)
            ;; active (- (count items) done)
            ]
        [:div
         [:section#todoapp
          [:header#header
           [:h1 "todos"]
           #_[todo-input {:id "new-todo"
                          :placeholder "What needs to be done?"
                          :on-save add-todo}]]
          (when (-> items count pos?)
            [:div
             [:section#main
              #_[:input#toggle-all {:type "checkbox" :checked (zero? active)
                                    :on-change #(complete-all (pos? active))}]
              [:label {:for "toggle-all"} "Mark all as complete"]
              [:ul#todo-list
               (for [todo items]
                 ^{:key (:db/id todo)} [todo-item todo])]]
             [:footer#footer
              [todo-stats]]])]
         [:footer#info
          [:p "Double-click to edit a todo"]]]))))
