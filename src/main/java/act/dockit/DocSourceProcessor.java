package act.dockit;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Defines a general contact needs to be implemented by a
 * real document source processor
 */
public interface DocSourceProcessor {
    /**
     * Process the content and return the process result.
     *
     * In general it shall parse and process the raw source e.g. a markdown
     * and return HTML formatted document
     *
     * @param source the doc source
     * @return the processed result
     */
    String process(String source);

    /**
     * Returns file suffixes this processor can process.
     * @return a list of file suffixes
     */
    List<String> interestedSuffixes();
}
