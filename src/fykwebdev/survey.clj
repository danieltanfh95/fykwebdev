(ns fykwebdev.survey
  (:require [compojure.api.sweet :refer [GET POST PUT DELETE]]
            [fykwebdev.models.survey :refer [Survey]]
            [fykwebdev.util :as str]
            [ring.util.http-response :refer [ok not-found created]]
            [schema.core :as s]
            [toucan.db :as db]))

(s/defschema SurveySchemaExpectation
  {:studentname s/Str
   :expectation s/Str})

(s/defschema SurveySchemaFirstDayFeedback
  {:studentname s/Str
   :firstdayfeedback s/Str})

(s/defschema SurveySchemaSecondDayFeedback
  {:studentname s/Str
   :seconddayfeedback s/Str})

(defn valid-student-id? [id]
  (str/non-blank-with-max-length? 6 id))

(defn upsert [id req]
  (or (db/update! Survey id req)
      (db/insert! Survey req)))


(defn create-survey-handler [req student-id]
  (->> req
       (#(assoc % :id student-id))
       (upsert student-id))
  (ok))

(def survey-routes
  [(POST "/survey/:student-id/expectation" []
         :path-params [student-id :- (s/constrained s/Str valid-student-id?)]
         :body [req SurveySchemaExpectation]
         (create-survey-handler req student-id))
   (POST "/survey/:student-id/firstDayFeedback" []
         :path-params [student-id :- (s/constrained s/Str valid-student-id?)]
         :body [req SurveySchemaFirstDayFeedback]
         (create-survey-handler req student-id))
   (POST "/survey/:student-id/secondDayFeedback" []
         :path-params [student-id :- (s/constrained s/Str valid-student-id?)]
         :body [req SurveySchemaSecondDayFeedback]
         (create-survey-handler req student-id))])