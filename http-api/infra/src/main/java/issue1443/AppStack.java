package issue1443;

import io.micronaut.aws.cdk.function.MicronautFunction;
import io.micronaut.aws.cdk.function.MicronautFunctionFile;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.alpha.PayloadFormatVersion;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Tracing;
import software.constructs.Construct;
import java.util.HashMap;
import java.util.Map;

import static software.amazon.awscdk.services.apigatewayv2.alpha.PayloadFormatVersion.VERSION_1_0;

public class AppStack extends Stack {

    public AppStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public AppStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        Map<String, String> environmentVariables = new HashMap<>();
        // https://aws.amazon.com/blogs/compute/optimizing-aws-lambda-function-performance-for-java/
        environmentVariables.put("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
        Function function = MicronautFunction.create(ApplicationType.DEFAULT,
                false,
                this,
                "micronaut-function")
                .environment(environmentVariables)
                .code(Code.fromAsset(functionPath()))
                .timeout(Duration.seconds(10))
                .memorySize(512)
                .tracing(Tracing.ACTIVE)
                .build();

        HttpLambdaIntegration integration = HttpLambdaIntegration.Builder.create("HttpLambdaIntegration", function)
            .payloadFormatVersion(VERSION_1_0)
                .build();
        HttpApi api = HttpApi.Builder.create(this, "micronaut-function-api")
                .defaultIntegration(integration)
                .build();
        CfnOutput.Builder.create(this, "MnIssueHttpApiUrl")
                .exportName("MnIssueHttpApiUrl")
                .value(api.getUrl())
                .build();
    }

    public static String functionPath() {
        return "../app/build/libs/" + functionFilename();
    }

    public static String functionFilename() {
        return MicronautFunctionFile.builder()
            .graalVMNative(false)
            .version("0.1")
            .archiveBaseName("app")
            .buildTool(BuildTool.GRADLE)
            .build();
    }
}