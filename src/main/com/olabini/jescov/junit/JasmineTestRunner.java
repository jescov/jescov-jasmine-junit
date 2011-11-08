/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package com.olabini.jescov.junit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import be.klak.rhino.RhinoContext;

import com.olabini.jescov.Configuration;
import com.olabini.jescov.Coverage;
import com.olabini.jescov.CoverageData;
import com.olabini.jescov.generators.JsonIngester;
import com.olabini.jescov.generators.JsonGenerator;

public class JasmineTestRunner extends be.klak.junit.jasmine.JasmineTestRunner {
    private Configuration configuration;
    private Coverage coverage;

	public JasmineTestRunner(Class<?> testClass) {
        super(testClass);
	}

    @Override
    protected void pre(RhinoContext context) {
        this.configuration = ConfigurationFactory.getConfiguration(suiteAnnotation);
        this.coverage = Coverage.on(context.getJsContext(), context.getJsScope(), configuration);
    }

    @Override
    protected void after() {
        this.coverage.done();
        super.after();
        outputCoverageResults();
    }

    private void outputCoverageResults() {
        if(configuration.isEnabled()) {
            CoverageData data = coverage.getCoverageData();
            String fileout = configuration.getJsonOutputFile();

            try {
                if(configuration.isJsonOutputMerge() && new File(fileout).exists()) {
                    FileReader fr = new FileReader(fileout);
                    CoverageData moreData = new JsonIngester().ingest(fr);
                    fr.close();
                    data = moreData.plus(data);
                }

                FileWriter fw = new FileWriter(fileout);
                new JsonGenerator(fw).generate(data);
                fw.close();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
