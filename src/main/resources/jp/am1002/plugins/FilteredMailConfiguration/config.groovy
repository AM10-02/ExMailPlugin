package jp.am1002.plugins.FilteredMailConfiguration

f = namespace(lib.FormTagLib)

f.section(title:_("Filtered Mail Plugin")) {
    f.entry(title: "Filter", field: "filter") {
        f.textbox()
    }
}