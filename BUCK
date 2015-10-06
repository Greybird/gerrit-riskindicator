include_defs('//bucklets/gerrit_plugin.bucklet')

MODULE = 'com.googlesource.gerrit.plugins.riskindicator.HelloForm'

gerrit_plugin(
  name = 'riskindicator-plugin',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/**/*']),
  gwt_module = MODULE,
  manifest_entries = [
    'Gerrit-PluginName: riskindicator',
    'Gerrit-Module: com.googlesource.gerrit.plugins.riskindicator.Module',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.riskindicator.HttpModule',
    'Gerrit-SshModule: com.googlesource.gerrit.plugins.riskindicator.SshModule',
    'Implementation-Title: Risk Indicator plugin',
    'Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/riskindicator-plugin',
  ]
)

java_test(
  name = 'riskindicator_tests',
  srcs = glob(['src/test/java/**/*.java']),
  deps = [
    ':riskindicator-plugin__plugin',
    '//lib:junit',
  ],
)

# TODO(davido): is this needed?
# requires for bucklets/tools/eclipse/project.py to work
# not sure, if this does something useful in standalone context
java_library(
  name = 'classpath',
  deps = [':riskindicator-plugin__plugin'],
)
