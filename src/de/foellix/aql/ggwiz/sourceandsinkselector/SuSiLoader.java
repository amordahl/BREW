package de.foellix.aql.ggwiz.sourceandsinkselector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Parameter;
import de.foellix.aql.ggwiz.Data;
import de.foellix.aql.helper.Helper;

public class SuSiLoader {
	private static final String SUSI_FILE = "data/SourcesAndSinks.txt";

	private static SuSiLoader instance = new SuSiLoader();
	private final List<String> sources;
	private final List<String> sinks;
	private final List<String> ignore;

	private SuSiLoader() {
		this.sources = new ArrayList<>();
		this.sinks = new ArrayList<>();
		this.ignore = new ArrayList<>();
		load();
	}

	public static SuSiLoader getInstance() {
		return instance;
	}

	private void load() {
		// Read SuSi file
		final Path susiFile = new File(SUSI_FILE).toPath();
		try {
			final List<String> lines = Files.readAllLines(susiFile, Charset.forName("UTF-8"));
			for (final String line : lines) {
				final String stmStr = Helper.cutFromFirstToLast(
						line.substring(0, (line.lastIndexOf(">") > 0 ? line.lastIndexOf(">") : line.length())), "<",
						">");
				if (!line.startsWith("%")) {
					if (line.contains("_SOURCE_")) {
						this.sources.add(stmStr);
					}
					if (line.contains("_SINK_")) {
						this.sinks.add(stmStr);
					}
				} else if (line.contains("_SOURCE_") || line.contains("_SINK_")) {
					this.ignore.add(stmStr);
				}
			}
			Log.msg("Loaded " + this.sources.size() + " sources and " + this.sinks.size() + " sinks.",
					Log.DEBUG_DETAILED);
		} catch (final IOException e) {
			Log.msg("Could not load SuSi File (" + susiFile.toString() + ").", Log.DEBUG);
		}
	}

	public void apply() {
		for (final SourceOrSink item : Data.getInstance().getSourcesAndSinks()) {
			// is source
			if (this.sources.contains(item.getReference().getStatement().getStatementgeneric())) {
				item.setSource(true);
			}
			// is sink
			if (this.sinks.contains(item.getReference().getStatement().getStatementgeneric())) {
				boolean hasOpenParameter = false;
				for (final Parameter para : item.getReference().getStatement().getParameters().getParameter()) {
					if (para.getValue().contains("$")) {
						hasOpenParameter = true;
						break;
					}
				}
				if (hasOpenParameter) {
					item.setSink(true);
				}
			}
		}
	}

	public List<String> getIgnore() {
		return this.ignore;
	}
}
