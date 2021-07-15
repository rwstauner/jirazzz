# jirazzz

A jira rest client command line app written as a [babashka][babashka] script.

Designed for configurability, to be usable with any jira instance.

Maybe you can't configure cli authentication, or don't want to use basic auth...
Just visit jira in your browser and steal
your own cookie (see "Example Config" below).


## Usage

    jirazzz --help

<!-- { jirazzz help -->
...
<!-- jirazzz help } -->


## Configuration

The default file is `~/.jirazzz.edn`.
An alternate can be specified with env var `JIRAZZZ_CONFIG_FILE`.

<!-- { jirazzz example-config -->

...

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
