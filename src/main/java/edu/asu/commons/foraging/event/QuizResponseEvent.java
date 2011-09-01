package edu.asu.commons.foraging.event;

import java.util.List;
import java.util.Properties;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.event.ClientRequest;
import edu.asu.commons.net.Identifier;

/**
 * $Id: QuizResponseEvent.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * A client's quiz responses for a given quiz page.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 522 $
 */
public class QuizResponseEvent extends AbstractPersistableEvent implements ClientRequest {

    private static final long serialVersionUID = -7081410122722056083L;

    private Properties responses;

    private List<String> incorrectAnswers;

    public QuizResponseEvent(Identifier id, Properties responses, List<String> incorrectAnswers) {
        super(id);
        this.responses = responses;
        this.incorrectAnswers = incorrectAnswers;
    }

    public Properties getResponses() {
        return responses;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    @Override
    public String toString() {
        return String.format("%s, responses: %s, incorrect answers: %s", id, responses, incorrectAnswers);
    }

    public int getNumberOfCorrectAnswers() {
        // FIXME: kludgy - responses is always off by one as it also contains the input submit button.
        int correctAnswers = (responses.size() - 1) - incorrectAnswers.size();
        if (correctAnswers < 0) {
            // FIXME: replace with proper logging?
            System.err.println("Somehow the number of responses was less than the number of incorrect answers: "
                    + responses + " -- " + incorrectAnswers);
            return 0;
        }
        return correctAnswers;
    }
}
