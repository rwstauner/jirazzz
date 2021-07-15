(ns jirazzz.transition-test
  (:require
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest transition
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:transitions tu/responses))

  (let [r (jirazzz transition
                   --issue "JZ-123"
                   --transition "done")]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "JZ-123 moved to done\n"
           (:out r))
        (:err r)))

  (is (= [{:uri (:meta tu/jira-paths)
           :method "get"
           :query-string "projectKeys=JZ"}
          ; TODO don't get sprint if we aren't going to use it.
          {:uri (:sprint tu/jira-paths)
           :method "get"}
          {:uri (:transitions tu/jira-paths)
           :method "get"}
          {:uri (:transitions tu/jira-paths)
           :method "post"
           :body
           {:transition
            {:id 3}}}]
         (tu/requests))))
