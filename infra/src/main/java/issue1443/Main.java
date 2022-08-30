package issue1443;

import software.amazon.awscdk.App;

public class Main {
    public static void main(final String[] args) {
        App app = new App();
        new AppStack(app, "MicronautIssueStack");
        app.synth();
    }
}