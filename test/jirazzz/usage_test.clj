(ns jirazzz.usage-test
  (:require
    [clojure.string :as string]
    [clojure.test :refer [deftest testing is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest usage
  (testing "help"
    (let [r (jirazzz --help)
          out (:err r)]
      (is (= 0
             (:exit r)))
      (is (re-find #"^Usage: jirazzz command.+"
                   out))
      (is (= (-> (re-find #"(?ms:^Commands:\n\n(.+?)\n\n)" out)
                 second
                 (string/split-lines)
                 (->> (map #(-> (re-find #"^\s+(\S+)\s+" %)
                                second))))
             ["assign"
              "commit-msg"
              "create"
              "issue"
              "parse-log"
              "transition"
              "get"
              "post"
              "put"
              "delete"])
          "documents commands in ranked order")))

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

  ; FIXME
  (is (= [{:uri (tu/jira-path :meta)
           :query-string "projectKeys=JZ"
           :method "get"}
          {:uri (tu/jira-path :sprint)
           :method "get"}]
         (tu/requests))
      "meta/sprint fetched before command is tried"))
