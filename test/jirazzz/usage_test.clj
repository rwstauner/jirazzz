(ns jirazzz.usage-test
  (:require
    [clojure.test :refer [deftest testing is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest usage
  (testing "help"
    (let [r (jirazzz --help)]
      (is (= 0
             (:exit r)))
      (is (re-find #"^Usage: jirazzz command.+"
                   (:err r)))))

  (testing "bad opts"
    (let [r (jirazzz create --thing)]
      (is (= 1
             (:exit r)))
      (is (re-find #"^Unknown option: \"--thing\""
                   (:err r)))
      (is (re-find #"(?m:^Usage: jirazzz )"
                   (:err r))
          "displays usage")))

  (is (= []
         (tu/requests))
      "no requests")

  (testing "too many args"
    (let [r (jirazzz create a thing)]
      (is (= 1
             (:exit r)))
      (is (re-find #"^Invalid arguments: .+\"a\" \"thing\""
                   (:err r)))
      (is (re-find #"(?m:^Usage: jirazzz )"
                   (:err r))
          "displays usage")))

  (is (= [{:uri (:meta tu/jira-paths)
           :query-string "projectKeys=JZ"
           :method "get"}
          {:uri (:sprint tu/jira-paths)
           :method "get"}]
         (tu/requests))
      "meta/sprint fetched before command is tried"))
