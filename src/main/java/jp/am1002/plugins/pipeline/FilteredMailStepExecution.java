package jp.am1002.plugins.pipeline;

import hudson.AbortException;
import hudson.model.TaskListener;
import jenkins.plugins.mailer.tasks.MimeMessageBuilder;
import jp.am1002.plugins.FilteredMailConfiguration;
import jp.am1002.plugins.Messages;
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
        if (StringUtils.isBlank(address)) {
            return true;
        }

        String filter = FilteredMailConfiguration.get().getFilter();
        if (StringUtils.isBlank(filter)) {
            return true;
        }

        List<String> approvalDomainList = Arrays.asList(filter.split(","));

        int index = address.indexOf("@");
        String domain = address.substring(index + 1);

        return approvalDomainList.contains(domain);
    }

    private String getApprovalDomain(Message.RecipientType recipientType, String domains, TaskListener listener) {
        List<String> approvalList = new ArrayList<>();
        for (String target : domains.split(",")) {
            if (isApproval(target)) {
                approvalList.add(target);
            } else {
                listener.getLogger().println(Messages.Execution_Unapproved(target));
            }
        }

        String approval = String.join(",", approvalList);

        listener.getLogger().println(
                StringUtils.isBlank(approval)
                        ? Messages.Execution_Approval_Empty(recipientType.toString())
                        : Messages.Execution_Approval(approval, recipientType.toString())
        );

        return approval;
    }

    private MimeMessage buildMimeMessage() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);

        if (StringUtils.isBlank(step.subject) || StringUtils.isBlank(step.body)) {
            throw new AbortException(Messages.Execution_Abort_Blank());
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
            String approval = getApprovalDomain(Message.RecipientType.TO, step.to, listener);
            allApproval &= step.to.equals(approval);
            messageBuilder.addRecipients(approval, Message.RecipientType.TO);
        }
        if (step.cc != null) {
            String approval = getApprovalDomain(Message.RecipientType.CC, step.cc, listener);
            allApproval &= step.cc.equals(approval);
            messageBuilder.addRecipients(approval, Message.RecipientType.CC);
        }
        if (step.bcc != null) {
            String approval = getApprovalDomain(Message.RecipientType.BCC, step.bcc, listener);
            allApproval &= step.bcc.equals(approval);
            messageBuilder.addRecipients(approval, Message.RecipientType.BCC);
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
            throw new AbortException(Messages.Execution_Abort_Recipients());
        }

        return message;
    }
}
