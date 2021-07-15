# jirazzz

A jira rest client command line app written as a [babashka][babashka] script.

Designed for configurability, to be usable with any jira instance.

Maybe you can't configure cli authentication, or don't want to use basic auth...
Just visit jira in your browser and steal
your own cookie (see "Example Config" below).


## Usage

    jirazzz --help

<!-- { jirazzz help -->

    Usage: jirazzz command [options]

    Commands:

      assign       Set assignee on issue: jirazzz assign --issue ABC-123 username
      commit-msg   Create issue from commit msg if no reference exists
      create       Create issue: jirazzz create --summary … --description …
      issue        Get issue: jirazzz issue KEY
      parse-log    Parse commit log and transition referenced issues
      transition   Transition issue: jirazzz transition --issue ABC-123 --transition 'done'
      get          Send GET    request: jirazzz get    /rest/api/x/y/z …
      post         Send POST   request: jirazzz post   /rest/api/x/y/z …
      put          Send PUT    request: jirazzz put    /rest/api/x/y/z …
      delete       Send DELETE request: jirazzz delete /rest/api/x/y/z …

    Options can be specified on the command line or in example-config.edn
    Any aliases defined in {:custom-fields {:your-name :customfield_00000}}
    can also be set in the config file or on the command line (see below).

    Options:
      -h, --help                  Show usage
          --assignee USERNAME     Assign issue to username (-1 for unassigned)
          --backlog               Put isue in backlog instead of current sprint
          --description DESC      Issue description
      -i, --issue KEY             Issue Key
      -p, --project PROJECT       Project for issue creation
          --rapid-view ID         RapidView id
          --summary SUMMARY       Issue summary
          --transition NAME       Transition issue to this state
          --type TYPE             Issue Type
          --url URL               Jira URL
          --input-format FORMAT   Set input format (can be 'json', default is 'edn')
          --output-format FORMAT  Set output format (can be 'json', default is 'edn')
          --sprint-id VALUE       Set custom field for sprint-id (customfield_12345)
          --storypoints VALUE     Set custom field for storypoints (customfield_10001)


<!-- jirazzz help } -->


## Configuration

The default file is `~/.jirazzz.edn`.
An alternate can be specified with env var `JIRAZZZ_CONFIG_FILE`.

<!-- { jirazzz example-config -->

```clojure
{
 :url "https://jira.example.com"
 :headers {"Cookie" #file ".cookie.txt"}
 :rapid-view 123
 :project "ABC"
 :assignee #env USER
 :issue-type "Task"
 :issue-pattern "\\[([A-Z]+-[0-9]+)\\]"
 :commit-template "[{{issue}}] {{summary}}\n\n{{description}}"
 :transition "ready for review"
 :storypoints 1
 :custom-fields
 {
  :sprint-id   :customfield_12345
  :storypoints :customfield_10001
 }
}
```

<!-- jirazzz example-config } -->

Most fields in the config file can be overridden via command line arguments
(see usage output).

Available readers:
    - `#env` - Substitute an environment variable
    - `#file` - Read file (relative to config file directory)

Need to figure out your custom fields?

Look at the json output for an existing issue
by inspecting the response in your browser, or use jirazzz:

    jirazzz issue ABC-123


## Pronunciation

Who cares.
Call it "jira Z" or "jira Z's".
I usually just say "jiras" in my head.


## Development

To update the docs:

    bb docs

[babashka]: https://babashka.org
