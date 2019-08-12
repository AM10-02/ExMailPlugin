package jp.am1002.plugins.pipeline;

import hudson.AbortException;
import hudson.model.TaskListener;
import jenkins.plugins.mailer.tasks.MimeMessageBuilder;
import jp.am1002.plugins.FilteredMailConfiguration;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilteredMailStepExecution extends SynchronousNonBlockingStepExecution<Void> {
    private static final Logger LOGGER = Logger.getLogger(FilteredMailStepExecution.class.getName());
    private static final long serialVersionUID = 1L;

    private transient final FilteredMailStep step;
    FilteredMailStepExecution(FilteredMailStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {
        MimeMessage mimeMessage = buildMimeMessage();
        Transport.send(mimeMessage);
        return null;
    }

    private boolean isApproval(String address) {
        LOGGER.log(Level.INFO, "Test Address => " + address);
        if (StringUtils.isBlank(address)) {
            return true;
        }

        String filter = FilteredMailConfiguration.get().getFilter();
        LOGGER.log(Level.INFO, "Test Filter => " + filter);
        if (StringUtils.isBlank(filter)) {
            return true;
        }

        List<String> approvalDomainList = Arrays.asList(filter.split(","));
        LOGGER.log(Level.INFO, "Test List => " + approvalDomainList);

        int index = address.indexOf("@");
        String domain = address.substring(index + 1);
        LOGGER.log(Level.INFO, "Test Domain => " + domain);

        return approvalDomainList.contains(domain);
    }

    private MimeMessage buildMimeMessage() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);

        if (StringUtils.isBlank(step.subject) || StringUtils.isBlank(step.body)) {
            throw new AbortException("Email not sent. All mandatory properties must be supplied ('subject', 'body').");
        }
        MimeMessageBuilder messageBuilder = new MimeMessageBuilder().setListener(getContext().get(TaskListener.class));

        boolean allApproval = true;

        if (step.subject != null) {
            messageBuilder.setSubject(step.subject);
        }
        if (step.body != null) {
            messageBuilder.setBody(step.body);
        }
        if (step.from != null) {
            messageBuilder.setFrom(step.from);
        }
        if (step.replyTo != null) {
            messageBuilder.setReplyTo(step.replyTo);
        }
        if (step.to != null) {
            List<String> approvalToList = new ArrayList<>();
            for (String targetTo : step.to.split(",")) {
                if (isApproval(targetTo)) {
                    approvalToList.add(targetTo);
                } else {
                    allApproval = false;
                    listener.getLogger().println(targetTo + "は許可されていません");
                }
            }
            listener.getLogger().println(String.join(",", approvalToList) + " To");
            messageBuilder.addRecipients(String.join(",", approvalToList), Message.RecipientType.TO);
        }
        if (step.cc != null) {
            List<String> approvalCcList = new ArrayList<>();
            for (String targetTo : step.cc.split(",")) {
                if (isApproval(targetTo)) {
                    approvalCcList.add(targetTo);
                } else {
                    allApproval = false;
                    listener.getLogger().println(targetTo + "は許可されていません");
                }
            }
            listener.getLogger().println(String.join(",", approvalCcList) + " Cc");
            messageBuilder.addRecipients(String.join(",", approvalCcList), Message.RecipientType.CC);
        }
        if (step.bcc != null) {
            List<String> approvalBccList = new ArrayList<>();
            for (String targetBcc : step.bcc.split(",")) {
                if (isApproval(targetBcc)) {
                    approvalBccList.add(targetBcc);
                } else {
                    allApproval = false;
                    listener.getLogger().println(targetBcc + "は許可されていません");
                }
            }
            listener.getLogger().println(String.join(",", approvalBccList) + " Bcc");
            messageBuilder.addRecipients(String.join(",", approvalBccList), Message.RecipientType.BCC);
        }
        if (step.getCharset() != null) {
            messageBuilder.setCharset(step.getCharset());
        }
        if (step.getMimeType() != null) {
            messageBuilder.setMimeType(step.getMimeType());
        }

        if (!allApproval && step.abortUnapproved) {
            throw new AbortException("Do not approved email.");
        }

        MimeMessage message = messageBuilder.buildMimeMessage();

        Address[] allRecipients = message.getAllRecipients();
        if (allRecipients == null || allRecipients.length == 0) {
            throw new AbortException("Email not sent. No recipients of any kind specified ('to', 'cc', 'bcc').");
        }

        return message;
    }
}
