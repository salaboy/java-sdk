/*
 * Copyright 2025 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package io.dapr.springboot.examples.producer;

import io.dapr.spring.workflows.config.EnableDaprWorkflows;
import io.dapr.springboot.examples.producer.wfp.chain.DemoChainWorkflow;
import io.dapr.springboot.examples.producer.wfp.child.DemoWorkflow;
import io.dapr.springboot.examples.producer.wfp.faninout.DemoFanInOutWorkflow;
import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RestController
@EnableDaprWorkflows
public class WorkflowPatternsRestController {

  private final Logger logger = LoggerFactory.getLogger(WorkflowPatternsRestController.class);

  @Autowired
  private DaprWorkflowClient daprWorkflowClient;

  /**
   * Run Chain Demo Workflow
   * @return confirmation that the workflow instance was created for the workflow pattern chain
   */
  @PostMapping("workflow-patterns/chain")
  public String chain() {
    String instanceId = daprWorkflowClient.scheduleNewWorkflow(DemoChainWorkflow.class);
    logger.info("Workflow instance " + instanceId + " started");
    return "New Workflow Instance created for workflow pattern: Chain Activities.";
  }

  /**
   * Run Child Demo Workflow
   * @return confirmation that the workflow instance was created for the workflow pattern child
   */
  @PostMapping("workflow-patterns/child")
  public String child() {
    String instanceId = daprWorkflowClient.scheduleNewWorkflow(DemoWorkflow.class);
    logger.info("Workflow instance " + instanceId + " started");
    return "New Workflow Instance created for workflow pattern: Child Workflows.";
  }


  /**
   * Run Fan In/Out Demo Workflow
   * @return confirmation that the workflow instance was created for the workflow pattern faninout
   */
  @PostMapping("workflow-patterns/faninout")
  public String faninout() throws TimeoutException {
    // The input is an arbitrary list of strings.
    List<String> listOfStrings = Arrays.asList(
            "Hello, world!",
            "The quick brown fox jumps over the lazy dog.",
            "If a tree falls in the forest and there is no one there to hear it, does it make a sound?",
            "The greatest glory in living lies not in never falling, but in rising every time we fall.",
            "Always remember that you are absolutely unique. Just like everyone else.");

    String instanceId = daprWorkflowClient.scheduleNewWorkflow(DemoFanInOutWorkflow.class, listOfStrings);
    logger.info("Workflow instance " + instanceId + " started");

    // Block until the orchestration completes. Then print the final status, which includes the output.
    WorkflowInstanceStatus workflowInstanceStatus = daprWorkflowClient.waitForInstanceCompletion(
            instanceId,
            Duration.ofSeconds(30),
            true);
    logger.info("workflow instance with ID: %s completed with result: %s%n", instanceId,
            workflowInstanceStatus.readOutputAs(int.class));
    return "Fan In/Out Workflows Demo finished";
  }

}

