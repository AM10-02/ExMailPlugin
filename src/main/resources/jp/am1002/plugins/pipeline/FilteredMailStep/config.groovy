package jp.am1002.plugins.pipeline.FilteredMailStep

f = namespace(lib.FormTagLib)

f.entry(title:_("To"), field: "to") {
    f.textbox()
}

f.entry(title:_("CC"), field: "cc") {
    f.textbox()
}

f.entry(title:_("BCC"), field: "bcc") {
    f.textbox()
}

f.entry(title:_("Subject"), field: "subject") {
    f.textbox()
}

f.entry(title:_("Body"), field: "body") {
    f.textarea()
}

f.entry(title:_("Abort Unapproved"), field: "abortUnapproved") {
    f.checkbox(default: "false")
}

f.advanced() {
    f.entry(title:_("From"), field: "from") {
        f.textbox()
    }

    f.entry(title:_("Reply-To"), field: "replyTo") {
        f.textbox()
    }

    f.entry(title:_("Body MIME Type"), field: "mimeType") {
        f.textbox()
    }

    f.entry(title:_("Body Character Set"), field: "charset") {
        f.textbox()
    }
}
