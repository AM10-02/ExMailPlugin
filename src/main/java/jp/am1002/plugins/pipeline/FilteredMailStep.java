package jp.am1002.plugins.pipeline;

import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import jp.am1002.plugins.Messages;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class FilteredMailStep extends Step {
    private String charset;

    public final String subject;
    public final String body;

    @DataBoundSetter
    public String from;
    @DataBoundSetter
    public String to;
    @DataBoundSetter
    public String cc;
    @DataBoundSetter
    public String bcc;
    @DataBoundSetter
    public String replyTo;
    @DataBoundSetter
    public boolean abortUnapproved;

    private String mimeType;

    @DataBoundConstructor
    public FilteredMailStep(@Nonnull String subject, @Nonnull String body) {
        this.subject = subject;
        this.body = body;
    }

    @DataBoundSetter
    public void setCharset(String charset) {
        this.charset = Util.fixEmpty(charset);
    }

    public String getCharset() {
        return charset;
    }

    @DataBoundSetter
    public void setMimeType(String mimeType) {
        this.mimeType = Util.fixEmpty(mimeType);
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new FilteredMailStepExecution(this, stepContext);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "filteredMail";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }

        @CheckForNull
        @Override
        public String argumentsToString(@Nonnull Map<String, Object> namedArgs) {
            Object subject = namedArgs.get("subject");
            return subject instanceof String ? (String)subject : null;
        }
    }
}
