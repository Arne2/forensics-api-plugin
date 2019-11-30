package io.jenkins.plugins.forensics.miner;

import hudson.util.XStream2;

import io.jenkins.plugins.util.AbstractXmlStream;

/**
 * Reads {@link RepositoryStatistics} from an XML file.
 *
 * @author Ullrich Hafner
 */
class RepositoryStatisticsXmlStream extends AbstractXmlStream<RepositoryStatistics> {
    RepositoryStatisticsXmlStream() {
        super(RepositoryStatistics.class);
    }

    @Override
    protected RepositoryStatistics createDefaultValue() {
        return new RepositoryStatistics();
    }

    @Override
    protected XStream2 createStream() {
        XStream2 xStream2 = new XStream2();
        xStream2.alias("repo", RepositoryStatistics.class);
        xStream2.alias("file", FileStatistics.class);
        return xStream2;
    }

}
