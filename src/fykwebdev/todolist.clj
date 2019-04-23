(ns fykwebdev.todolist
  (:require [compojure.api.sweet :refer [GET POST PUT DELETE]]
            [fykwebdev.models.todolist :refer [TodoList]]
            [fykwebdev.util :as str]
            [ring.util.http-response :refer [ok not-found created]]
            [schema.core :as s]
            [toucan.db :as db]))

(defn valid-student-id? [id]
  (str/non-blank-with-max-length? 6 id))

(defn valid-description? [desc]
  (str/length-in-range? 1 255 desc))

(s/defschema TodoListSchema
  {:description (s/constrained s/Str valid-description?)})

(defn id->created [id]
  (created (str "/todolist/" id) {:id id}))

(defn create-todolist-handler [req student-id]
  (->> req
       (#(assoc % :student-id student-id))
       (db/insert! TodoList)
       :id
       id->created))

(defn get-todolist-handler [id]
  (ok (db/query {:select [:id :description]
                 :from [TodoList]
                 :where [:= :student-id id]
                 :order-by [:id]})))

(defn update-todolist-handler [req id]
  (db/update! TodoList id req)
  (ok))

(defn delete-todolist-handler [id]
  (db/delete! TodoList :id id)
  (ok))

(defn todolist->response [todolist]
  (if todolist
    (ok todolist)
    (not-found)))

(def todolist-routes
  [(POST "/todolist/:student-id" []
         :path-params [student-id :- (s/constrained s/Str valid-student-id?)]
         :body [req TodoListSchema]
         (create-todolist-handler req student-id))
   (GET "/todolist/:student-id" []
        :path-params [student-id :- (s/constrained s/Str valid-student-id?)]
        (get-todolist-handler student-id))
   (PUT "/todolist/:id" []
        :path-params [id :- s/Int]
        :body [req (dissoc TodoListSchema :student-id)]
        (update-todolist-handler req id))
   (DELETE "/todolist/:id" []
           :path-params [id :- s/Int]
           (delete-todolist-handler id))])