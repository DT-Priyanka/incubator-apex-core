/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.apex.stram;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.apex.stram.DeployRequest.EventGroupId;

import com.google.common.collect.Maps;

import com.datatorrent.stram.plan.physical.PTOperator;

/**
 * This class manages tracking ids of deploy/undeploy for containers and
 * operators.
 *
 */
public class DeployManager
{
  private static DeployManager deployManager = new DeployManager();
  private Map<String, DeployRequest> deployRequests = Maps.newHashMap();

  public static DeployManager getDeployManagerInstance()
  {
    return deployManager;
  }

  /**
   * Retruns all available deploy requests with StrAM
   * @return deployRequests
   */
  public Map<String, DeployRequest> getDeployRequests()
  {
    return deployRequests;
  }

  /**
   * Returns deploy request for container
   * @param containerId
   * @return deployRequest
   */
  public DeployRequest getDeployRequest(String containerId)
  {
    return deployRequests.get(containerId);
  }

  /**
   * Returns deploy/undeploy group Id for container
   * @param containerId
   * @return groupId <br/>
   *         <b>Note:</b> groupId 0 indicates and independent event, with no
   *         group
   */
  public EventGroupId getEventGroupIdForContainer(String containerId)
  {
    EventGroupId groupId = null;
    if (deployRequests.get(containerId) != null) {
      groupId = deployRequests.get(containerId).getEventGroupId();
    }
    return groupId;
  }

  /**
   * Returns deploy/undeploy group Id for container This could be a new
   * container allocated during redeploy process
   * @param containerId
   * @return groupId <br/>
   *         <b>Note:</b> groupId 0 indicates and indipendent event, with no
   *         group
   */
  public EventGroupId getEventGroupIdForAffectedContainer(String containerId)
  {
    EventGroupId groupId = getEventGroupIdForContainer(containerId);
    if (groupId != null) {
      return groupId;
    }
    for (DeployRequest request : getDeployRequests().values()) {
      if (request.getAffectedContainers().contains(containerId)) {
        groupId = request.getEventGroupId();
      }
    }
    return groupId;
  }

  /**
   * Returns deploy groupId for operator which is to undergo deploy. Operators
   * undergoing deploy for first time will have groupId as 0
   * @param operatorId
   * @return groupId <br/>
   *         <b>Note:</b> groupId 0 indicates and indipendent event, with no
   *         group
   */
  public EventGroupId getEventGroupIdForOperatorToDeploy(int operatorId)
  {
    for (DeployRequest request : getDeployRequests().values()) {
      if (request.getOperatorsToDeploy().contains(operatorId)) {
        return request.getEventGroupId();
      }
    }
    return null;
  }

  /**
   * Adds operator to deploy. The operator is added to request associated with containerId
   * @param containerIs
   * @param operator
   */
  public void addOperatorToDeploy(String containerId, PTOperator oper)
  {
    DeployRequest request = getDeployRequest(containerId);
    if (request != null) {
      request.addOperatorToDeploy(oper.getId());
    }
  }

  /**
   * Removes operator from deploy request
   */
  public boolean removeOperatorFromDeployRequest(int operatorId)
  {
    for (DeployRequest request : getDeployRequests().values()) {
      if (request.getOperatorsToDeploy().contains((operatorId))) {
        return request.removeOperatorToDeploy(operatorId);
      }
    }
    return false;
  }

  /**
   * Remove deployRequest from StrAM if it has no more pending operators to deploy
   * @param containerId
   */
  public void removeProcessedDeployRequests()
  {
    for (Entry<String, DeployRequest> request : deployRequests.entrySet()) {
      if (request.getValue().getOperatorsToDeploy().size() == 0
          && request.getValue().getOperatorsToUndeploy().size() == 0) {
        LOG.info("Removing for :" + request.getKey());
        deployRequests.remove(request.getKey());
      }
    }

  }

  /**
   * Create DeployRequest to group deploy/undeploy of related container/operator
   * events under one groupId to find related events.
   * To start will all related operators are added to opertorsToUndeploy list,
   * they will eventually move to operatorsToDeploy when operator undergo redeploy cycle.
   * @param containerId
   * @param affectedOperators
   */
  public DeployRequest addOrModifyDeployRequest(String containerId, Set<PTOperator> affectedOperators)
  {
    DeployRequest request = deployRequests.get(containerId);
    if (request == null) {
      request = new DeployRequest();
      deployRequests.put(containerId, request);
    }
    for (PTOperator oper : affectedOperators) {
      request.addOperatorToUndeploy(oper.getId());
      request.addAffectedContainer(oper.getContainer().getExternalId());
    }
    return request;
  }

  /**
   * Add affectedContainerId to deploy request, if container is deployed as part
   * of redeploy process of groupLeaderContainer
   * @param groupLeaderContainerId
   * @param affectedContainerId
   */
  public void addNewContainerToDeployRequest(String groupLeaderContainerId, String affectedContainerId)
  {
    if (groupLeaderContainerId != null && affectedContainerId != null) {
      DeployRequest request = getDeployRequest(groupLeaderContainerId);
      if (request != null) {
        request.addAffectedContainer(affectedContainerId);
      }
    }
  }

  /**
   * When operator state changes from PENDING_UNDEPLOY to PENDING_DEPLOY move
   * operator from operatorsToUndeploy to operatorsToDeploy
   * @param operator
   * @return groupId
   */
  public EventGroupId moveOperatorFromUndeployListToDeployList(PTOperator oper)
  {
    EventGroupId groupId = null;
    for (DeployRequest request : deployRequests.values()) {
      if (request.getOperatorsToUndeploy().contains(oper.getId())) {
        groupId = request.getEventGroupId();
        request.removeOperatorToUndeploy(oper.getId());
        request.addOperatorToDeploy(oper.getId());
      }
    }
    return groupId;
  }

  /**
   * Clear all deploy requests
   */
  public void clearAllDeployRequests()
  {
    deployRequests.clear();
  }

  private static final Logger LOG = LoggerFactory.getLogger(DeployManager.class);
}
