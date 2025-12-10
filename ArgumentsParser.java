package listedTradeGen;

import org.apache.commons.cli.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

/**
 * Parses and validates command-line inputs.
 */
public final class ArgumentsParser {
    private static final Set<String> ALLOWED_MARKETS = Set.of("EQUITY", "FUTURE", "OPTION");
    private static final Set<String> ALLOWED_REGIONS = Set.of("AMER", "EMEA", "APAC");

    private ArgumentsParser() {
    }

    public static GenerationRequest parse(String[] args) {
        Options options = new Options();
        options.addOption(required("m", "market", true, "Market sector: EQUITY | FUTURE | OPTION"));
        options.addOption(required("f", "flow", true, "Flow/source system identifier used to locate the template and topic (e.g. ZTBB, FIDESSA)"));
        options.addOption(required("r", "region", true, "Region: AMER | EMEA | APAC"));
        options.addOption(required("e", "env", true, "Environment, e.g. UAT1 or UAT2"));
        options.addOption(required("p", "prefix", true, "Prefix used for generated order and execution ids"));
        options.addOption(required("n", "count", true, "Number of messages to generate"));
        options.addOption(optional("s", "source-system", true, "Override the source system field inside the generated trades"));
        options.addOption(optional(null, "single-order", false, "Generate multiple executions for a single order. Without this flag each message gets a unique order."));
        options.addOption(optional(null, "keytab", true, "Path to a keytab file. Defaults to the bundled keytab resource if omitted."));
        options.addOption(optional(null, "principal", true, "Kerberos principal to use. Defaults to the Solace username for the chosen environment."));
        options.addOption(optional(null, "krb5", true, "Path to a krb5.conf file. Defaults to the bundled krb5.conf if omitted."));

        CommandLine cmd = parseCommandLine(args, options);

        String market = validateChoice(cmd.getOptionValue("m").trim(), ALLOWED_MARKETS, "market");
        String region = validateChoice(cmd.getOptionValue("r").trim(), ALLOWED_REGIONS, "region");
        String environment = cmd.getOptionValue("e").trim().toUpperCase(Locale.ROOT);
        String flow = cmd.getOptionValue("f").trim().toUpperCase(Locale.ROOT);
        String prefix = cmd.getOptionValue("p").trim();
        if (prefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be empty");
        }
        String sourceSystem = cmd.getOptionValue("s");
        if (sourceSystem != null) {
            sourceSystem = sourceSystem.trim();
        }
        boolean singleOrder = cmd.hasOption("single-order");
        int count = parseCount(cmd.getOptionValue("n"));

        Path keytabPath = cmd.hasOption("keytab") ? Paths.get(cmd.getOptionValue("keytab").trim()) : null;
        Path krb5Path = cmd.hasOption("krb5") ? Paths.get(cmd.getOptionValue("krb5").trim()) : null;
        String principal = cmd.getOptionValue("principal");
        if (principal != null) {
            principal = principal.trim();
        }

        return new GenerationRequest(
                market,
                flow,
                region,
                environment,
                singleOrder,
                prefix,
                count,
                sourceSystem,
                principal,
                keytabPath,
                krb5Path
        );
    }

    private static CommandLine parseCommandLine(String[] args, Options options) {
        try {
            CommandLineParser parser = new BasicParser();
            return parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("proto-processor", options, true);
            throw new IllegalArgumentException("Invalid arguments: " + e.getMessage(), e);
        }
    }

    private static String validateChoice(String value, Set<String> allowed, String name) {
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported " + name + ": " + value + " (allowed: " + allowed + ")");
        }
        return normalized;
    }

    private static int parseCount(String raw) {
        try {
            int parsed = Integer.parseInt(raw);
            if (parsed <= 0) {
                throw new IllegalArgumentException("Count must be greater than zero");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Count must be a number", e);
        }
    }

    private static Option required(String opt, String longOpt, boolean hasArg, String desc) {
        Option option = new Option(opt, longOpt, hasArg, desc);
        option.setRequired(true);
        return option;
    }

    private static Option optional(String opt, String longOpt, boolean hasArg, String desc) {
        Option option = new Option(opt, longOpt, hasArg, desc);
        option.setRequired(false);
        return option;
    }
}
