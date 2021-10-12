(ns jirazzz.create-test
  (:require
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest minimal
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:create tu/responses))

  (let [r (tu/with-test-config
            "none"
            (jirazzz create
                     --url tu/url
                     --project "JZ"
                     --assignee "me"
                     --issue-type "Task"
                     --summary "hi there"
                     --transition "" ; none
                     --description "meh"))]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "JZ-123\n"
           (:out r))
        (:err r)))

  (is (= [{:uri (tu/jira-path :meta)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (tu/jira-path :create)
           :method "post"
           :body
           {:fields
            {:summary "hi there"
             :description "meh"
             :project {:id 123456}
             :issuetype {:id 1}
             :assignee {:name "me"}}}}]
         (tu/requests))))

(deftest everything
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:create tu/responses))
  (tu/respond (:comment tu/responses))
  (tu/respond (:transitions tu/responses))

  (let [r (jirazzz create
                   --transition "ready for review"
                   --summary "hi there"
                   --description "meh"
                   --comment "one more thing"
                   --acceptance-criteria "whatever")]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "JZ-123\n"
           (:out r))
        (:err r)))

  (is (= [{:uri (tu/jira-path :meta)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (tu/jira-path :sprint)
           :method "get"}
          {:uri (tu/jira-path :create)
           :method "post"
           :body
           {:fields
            {:summary "hi there"
             :description "meh"
             :project {:id 123456}
             :issuetype {:id 1}
             :assignee {:name "-1"}
             :customfield_12345 2
             :customfield_23456 "whatever"}}}
          {:uri (tu/jira-path :comment)
           :method "post"
           :body {:body "one more thing"}}
          {:uri (tu/jira-path :transitions)
           :method "get"}
          {:uri (tu/jira-path :transitions)
           :method "post"
           :body
           {:transition
            {:id 2}}}]
         (tu/requests))))
