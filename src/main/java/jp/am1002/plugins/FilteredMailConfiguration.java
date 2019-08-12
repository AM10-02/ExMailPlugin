package jp.am1002.plugins;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Extension
public class FilteredMailConfiguration extends GlobalConfiguration {
    public static FilteredMailConfiguration get() {
        return GlobalConfiguration.all().get(FilteredMailConfiguration.class);
    }

    private String filter;

    public FilteredMailConfiguration() {
        load();
    }

    public String getFilter() {
        return filter;
    }

    @DataBoundSetter
    public void setFilter(String filter) {
        this.filter = filter;
        save();
    }

    public FormValidation doCheckFilter(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.ok();
        }
        String[] domains = value.split(",");
        Pattern pattern = Pattern.compile("[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]+");

        List<String> errors = new ArrayList<String>();

        for (String domain : domains) {
            if (!pattern.matcher(domain).find()) {
                errors.add(domain);
            }
        }

        if (errors.size() > 0) {
            return FormValidation.error(String.join(",", errors) + "はメールアドレスのドメインではありません");
        }


        return FormValidation.ok();
    }
}
