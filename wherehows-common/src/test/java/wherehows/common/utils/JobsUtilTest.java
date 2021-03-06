/**
 * Copyright 2015 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package wherehows.common.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.Test;
import wherehows.common.Constant;

import static wherehows.common.utils.JobsUtil.*;


public class JobsUtilTest {

  @Test
  public void testEnvVarResolution() throws IOException {
    String propertyStr = "var1=foo\n" + "var2=$JAVA_HOME\n" + "var3=${JAVA_HOME}";

    Path path = createPropertiesFile(propertyStr);

    Properties props = getResolvedProperties(path);

    Assert.assertNotEquals(props, null);
    Assert.assertEquals(props.getProperty("var1", ""), "foo");
    Assert.assertTrue(props.getProperty("var2").length() > 0);
    Assert.assertEquals(props.getProperty("var2"), props.getProperty("var3"));

    Files.deleteIfExists(path);
  }

  @Test
  public void testGetScheduledJobs() throws IOException {
    String propertyStr1 = "job.class=test\n" + "job.cron.expr=0 0 1 * * ? *\n" + "#job.disabled=1\n" + "job.type=TEST1";
    String propertyStr2 = "job.class=test\n" + "job.cron.expr=0 0 1 * * ? *\n" + "job.disabled=1\n" + "job.type=TEST2";
    String propertyStr3 = "job.class=test\n" + "#job.disabled=1\n" + "job.type=TEST3";

    Path path1 = createPropertiesFile(propertyStr1);
    Path path2 = createPropertiesFile(propertyStr2);
    Path path3 = createPropertiesFile(propertyStr3);

    String filename1 = jobNameFromPath(path1);

    String dir = path1.getParent().toString();

    Map<String, Properties> jobs = getScheduledJobs(dir);

    Assert.assertEquals(jobs.size(), 1);
    Assert.assertEquals(jobs.get(filename1).getProperty(Constant.JOB_TYPE_KEY), "TEST1");
    Assert.assertEquals(jobs.get(filename1).getProperty("job.class"), "test");

    Files.deleteIfExists(path1);
    Files.deleteIfExists(path2);
    Files.deleteIfExists(path3);
  }

  @Test
  public void testGetEnabledJobs() throws IOException {
    String propertyStr1 = "job.class=test\n" + "job.cron.expr=0 0 1 * * ? *\n" + "#job.disabled=1\n" + "job.type=TEST1";
    String propertyStr2 = "job.class=test\n" + "#job.disabled=1\n";

    Path path1 = createPropertiesFile(propertyStr1);
    Path path2 = createPropertiesFile(propertyStr2);

    String filename1 = jobNameFromPath(path1);
    String filename2 = jobNameFromPath(path2);

    String dir = path1.getParent().toString();

    Map<String, Properties> jobs = getEnabledJobs(dir);

    Assert.assertEquals(jobs.size(), 2);
    Assert.assertEquals(jobs.get(filename1).getProperty("job.class"), "test");
    Assert.assertEquals(jobs.get(filename1).getProperty("job.disabled", ""), "");
    Assert.assertEquals(jobs.get(filename2).getProperty("job.class"), "test");

    Files.deleteIfExists(path1);
    Files.deleteIfExists(path2);
  }

  @Test
  public void testGetEnabledJobsByType() throws IOException {
    String propertyStr1 = "job.class=test\n" + "job.cron.expr=0 0 1 * * ? *\n" + "#job.disabled=1\n" + "job.type=TEST1";
    String propertyStr2 = "job.class=test\n" + "job.cron.expr=0 0 1 * * ? *\n" + "#job.disabled=1\n" + "job.type=TEST2";

    Path path1 = createPropertiesFile(propertyStr1);
    Path path2 = createPropertiesFile(propertyStr2);

    String filename1 = jobNameFromPath(path1);

    String dir = path1.getParent().toString();

    Map<String, Properties> jobs = getEnabledJobsByType(dir, "TEST1");

    Assert.assertEquals(jobs.size(), 1);
    Assert.assertEquals(jobs.get(filename1).getProperty("job.class"), "test");
    Assert.assertEquals(jobs.get(filename1).getProperty("job.disabled", ""), "");

    Files.deleteIfExists(path1);
    Files.deleteIfExists(path2);
  }

  private Path createPropertiesFile(String content) throws IOException {
    File propertyFile = File.createTempFile("temp", ".job");
    FileWriter writer = new FileWriter(propertyFile);
    writer.write(content);
    writer.close();
    return propertyFile.toPath();
  }
}
