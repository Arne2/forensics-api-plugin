package io.jenkins.plugins.forensics.blame;

import java.util.Collection;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.util.FilteredLog;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.blame.FileBlame.FileBlameBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static io.jenkins.plugins.util.PathStubs.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link BlamerFactory}.
 *
 * @author Ullrich Hafner
 */
public class BlamerFactoryITest {
    /** Jenkins rule per suite. */
    @ClassRule
    public static final JenkinsRule JENKINS_PER_SUITE = new JenkinsRule();

    private static final String FILE_NAME = "file";
    private static final FilteredLog LOG = new FilteredLog("Foo");

    /** Verifies that different {@link Blamer} instances are created based on the stubbed workspace name. */
    @Test
    public void shouldSelectBlamerBasedOnWorkspaceName() {
        Blamer nullBlamer = createBlamer("/");

        assertThat(nullBlamer).isInstanceOf(NullBlamer.class);
        assertThat(nullBlamer.blame(new FileLocations(), LOG)).isEmpty();

        Blamer testBlamer = createBlamer("/test");
        assertThat(testBlamer).isInstanceOf(TestBlamer.class);
        assertThat(testBlamer.blame(new FileLocations(), LOG)).isNotEmpty();
        assertThat(testBlamer.blame(new FileLocations(), LOG)).hasFiles(FILE_NAME);

        Collection<FilePath> directories = asSourceDirectories(createWorkspace("/"), createWorkspace("/test"));
        Blamer testBlamerSecondMatch = BlamerFactory.findBlamer(mock(Run.class), directories, TaskListener.NULL, LOG);
        assertThat(testBlamerSecondMatch).isInstanceOf(TestBlamer.class);
        assertThat(testBlamerSecondMatch.blame(new FileLocations(), LOG)).isNotEmpty();
        assertThat(testBlamerSecondMatch.blame(new FileLocations(), LOG)).hasFiles(FILE_NAME);
    }

    private Blamer createBlamer(final String path) {
        return BlamerFactory.findBlamer(mock(Run.class), asSourceDirectories(createWorkspace(path)), TaskListener.NULL, LOG);
    }

    /**
     * Factory that does not return a blamer.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class EmptyFactory extends BlamerFactory {
        @Override
        public Optional<Blamer> createBlamer(final SCM scm, final Run<?, ?> run, final FilePath workspace,
                final TaskListener listener, final FilteredLog logger) {
            return Optional.empty();
        }
    }

    /**
     * Factory that returns a blamer if the workspace contains the String {@code test}.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class ActualFactory extends BlamerFactory {
        @Override
        public Optional<Blamer> createBlamer(final SCM scm, final Run<?, ?> run,
                final FilePath workspace, final TaskListener listener, final FilteredLog logger) {
            if (workspace.getRemote().contains("test")) {
                return Optional.of(new TestBlamer());
            }
            return Optional.empty();
        }
    }

    /** A blamer for the test. */
    private static class TestBlamer extends Blamer {
        private static final long serialVersionUID = -2091805649078555383L;

        @Override
        public Blames blame(final FileLocations fileLocations, final FilteredLog logger) {
            Blames blames = new Blames();
            blames.add(new FileBlameBuilder().build(FILE_NAME));
            return blames;
        }
    }
}
