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
package com.datatorrent.stram.api;

import java.util.concurrent.atomic.AtomicLong;

import com.datatorrent.stram.plan.logical.requests.LogicalPlanRequest;

/**
 * <p>
 * Abstract StramEvent class.</p>
 *
 * @since 0.9.2
 */
public abstract class StramEvent
{
  private static final AtomicLong nextId = new AtomicLong(1);
  private final long id;
  private long timestamp = System.currentTimeMillis();
  private String reason;
  private LogLevel logLevel;

  public abstract String getType();

  protected StramEvent(LogLevel logLevel)
  {
    id = nextId.getAndIncrement();
    this.logLevel = logLevel;
  }

  public long getId()
  {
    return id;
  }

  public long getTimestamp()
  {
    return timestamp;
  }

  public void setTimestamp(long timestamp)
  {
    this.timestamp = timestamp;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason(String reason)
  {
    this.reason = reason;
  }

  public LogLevel getLogLevel()
  {
    return logLevel;
  }

  public static enum LogLevel
  {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL;
  }

  public abstract static class OperatorEvent extends StramEvent
  {
    private String operatorName;

    public OperatorEvent(String operatorName, LogLevel logLevel)
    {
      super(logLevel);
      this.operatorName = operatorName;
    }

    public String getOperatorName()
    {
      return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
      this.operatorName = operatorName;
    }

  }

  public static class SetOperatorPropertyEvent extends OperatorEvent
  {
    private String propertyName;
    private String propertyValue;

    public SetOperatorPropertyEvent(String operatorName, String propertyName, String propertyValue)
    {
      this(operatorName, propertyName, propertyValue, LogLevel.INFO);
    }

    public SetOperatorPropertyEvent(String operatorName, String propertyName, String propertyValue, LogLevel logLevel)
    {
      super(operatorName, logLevel);
      this.propertyName = propertyName;
      this.propertyValue = propertyValue;
    }

    @Override
    public String getType()
    {
      return "SetOperatorProperty";
    }

    public String getPropertyName()
    {
      return propertyName;
    }

    public void setPropertyName(String propertyName)
    {
      this.propertyName = propertyName;
    }

    public String getPropertyValue()
    {
      return propertyValue;
    }

    public void setPropertyValue(String propertyValue)
    {
      this.propertyValue = propertyValue;
    }

  }

  public static class PartitionEvent extends OperatorEvent
  {
    private int oldNumPartitions;
    private int newNumPartitions;

    public PartitionEvent(String operatorName, int oldNumPartitions, int newNumPartitions)
    {
      this(operatorName, oldNumPartitions, newNumPartitions, LogLevel.INFO);
    }

    public PartitionEvent(String operatorName, int oldNumPartitions, int newNumPartitions, LogLevel logLevel)
    {
      super(operatorName, logLevel);
      this.oldNumPartitions = oldNumPartitions;
      this.newNumPartitions = newNumPartitions;
    }

    @Override
    public String getType()
    {
      return "Partition";
    }

    public int getOldNumPartitions()
    {
      return oldNumPartitions;
    }

    public void setOldNumPartitions(int oldNumPartitions)
    {
      this.oldNumPartitions = oldNumPartitions;
    }

    public int getNewNumPartitions()
    {
      return newNumPartitions;
    }

    public void setNewNumPartitions(int newNumPartitions)
    {
      this.newNumPartitions = newNumPartitions;
    }

  }

  public abstract static class PhysicalOperatorEvent extends OperatorEvent
  {
    private final int operatorId;

    public PhysicalOperatorEvent(String operatorName, int operatorId, LogLevel logLevel)
    {
      super(operatorName, logLevel);
      this.operatorId = operatorId;
    }

    public int getOperatorId()
    {
      return operatorId;
    }

  }

  public static class CreateOperatorEvent extends PhysicalOperatorEvent
  {
    public CreateOperatorEvent(String operatorName, int operatorId)
    {
      this(operatorName, operatorId, LogLevel.INFO);
    }

    public CreateOperatorEvent(String operatorName, int operatorId, LogLevel logLevel)
    {
      super(operatorName, operatorId, logLevel);
    }

    @Override
    public String getType()
    {
      return "CreateOperator";
    }

  }

  public static class RemoveOperatorEvent extends PhysicalOperatorEvent
  {
    public RemoveOperatorEvent(String operatorName, int operatorId)
    {
      this(operatorName, operatorId, LogLevel.INFO);
    }

    public RemoveOperatorEvent(String operatorName, int operatorId, LogLevel logLevel)
    {
      super(operatorName, operatorId, logLevel);
    }

    @Override
    public String getType()
    {
      return "RemoveOperator";
    }

  }

  public static class StartOperatorEvent extends PhysicalOperatorEvent
  {
    private String containerId;
    private long failureId;

    public StartOperatorEvent(String operatorName, int operatorId, String containerId, long failureId)
    {
      this(operatorName, operatorId, containerId, LogLevel.INFO, failureId);
    }

    public StartOperatorEvent(String operatorName, int operatorId, String containerId, LogLevel logLevel, long failureId)
    {
      super(operatorName, operatorId, logLevel);
      this.containerId = containerId;
      this.failureId = failureId;
    }

    @Override
    public String getType()
    {
      return "StartOperator";
    }

    public String getContainerId()
    {
      return containerId;
    }

    public void setContainerId(String containerId)
    {
      this.containerId = containerId;
    }

    public long getFailureId()
    {
      return failureId;
    }

    public void setFailureId(long failureId)
    {
      this.failureId = failureId;
    }
  }

  public static class StopOperatorEvent extends PhysicalOperatorEvent
  {
    private String containerId;
    private long failureId;

    public StopOperatorEvent(String operatorName, int operatorId, String containerId, long failureId)
    {
      this(operatorName, operatorId, containerId, LogLevel.WARN, failureId);
    }

    public StopOperatorEvent(String operatorName, int operatorId, String containerId, LogLevel logLevel, long failureId)
    {
      super(operatorName, operatorId, logLevel);
      this.containerId = containerId;
      this.failureId = failureId;
    }

    @Override
    public String getType()
    {
      return "StopOperator";
    }

    public String getContainerId()
    {
      return containerId;
    }

    public void setContainerId(String containerId)
    {
      this.containerId = containerId;
    }

    public long getFailureId()
    {
      return failureId;
    }

    public void setFailureId(long failureId)
    {
      this.failureId = failureId;
    }
  }

  public static class SetPhysicalOperatorPropertyEvent extends PhysicalOperatorEvent
  {
    private String propertyName;
    private String propertyValue;

    public SetPhysicalOperatorPropertyEvent(String operatorName, int operatorId, String propertyName, String propertyValue)
    {
      this(operatorName, operatorId, propertyName, propertyValue, LogLevel.INFO);
    }

    public SetPhysicalOperatorPropertyEvent(String operatorName, int operatorId, String propertyName, String propertyValue, LogLevel logLevel)
    {
      super(operatorName, operatorId, logLevel);
      this.propertyName = propertyName;
      this.propertyValue = propertyValue;
    }

    @Override
    public String getType()
    {
      return "SetPhysicalOperatorProperty";
    }

    public String getPropertyName()
    {
      return propertyName;
    }

    public void setPropertyName(String propertyName)
    {
      this.propertyName = propertyName;
    }

    public String getPropertyValue()
    {
      return propertyValue;
    }

    public void setPropertyValue(String propertyValue)
    {
      this.propertyValue = propertyValue;
    }

  }

  public static class StartContainerEvent extends StramEvent
  {
    String containerId;
    String containerNodeId;

    public StartContainerEvent(String containerId, String containerNodeId)
    {
      this(containerId, containerNodeId, LogLevel.INFO);
    }

    public StartContainerEvent(String containerId, String containerNodeId, LogLevel logLevel)
    {
      super(logLevel);
      this.containerId = containerId;
      this.containerNodeId = containerNodeId;
    }

    @Override
    public String getType()
    {
      return "StartContainer";
    }

    public String getContainerId()
    {
      return containerId;
    }

    public void setContainerId(String containerId)
    {
      this.containerId = containerId;
    }

    public String getContainerNodeId()
    {
      return containerNodeId;
    }

    public void setContainerNodeId(String containerNodeId)
    {
      this.containerNodeId = containerNodeId;
    }

  }

  public static class StopContainerEvent extends StramEvent
  {
    String containerId;
    int exitStatus;
    long failureId;

    public StopContainerEvent(String containerId, int exitStatus, long failureId)
    {
      this(containerId, exitStatus, LogLevel.INFO, failureId);
    }

    public StopContainerEvent(String containerId, int exitStatus, LogLevel logLevel, long failureId)
    {
      super(logLevel);
      this.containerId = containerId;
      this.exitStatus = exitStatus;
      this.failureId = failureId;
    }

    @Override
    public String getType()
    {
      return "StopContainer";
    }

    public String getContainerId()
    {
      return containerId;
    }

    public void setContainerId(String containerId)
    {
      this.containerId = containerId;
    }

    public int getExitStatus()
    {
      return exitStatus;
    }

    public void setExitStatus(int exitStatus)
    {
      this.exitStatus = exitStatus;
    }

    public long getFailureId()
    {
      return failureId;
    }

    public void setFailureId(long failureId)
    {
      this.failureId = failureId;
    }
  }

  public static class ChangeLogicalPlanEvent extends StramEvent
  {
    private LogicalPlanRequest request;

    public ChangeLogicalPlanEvent(LogicalPlanRequest request)
    {
      this(request, LogLevel.INFO);
    }

    public ChangeLogicalPlanEvent(LogicalPlanRequest request, LogLevel logLevel)
    {
      super(logLevel);
      this.request = request;
    }

    @Override
    public String getType()
    {
      return "ChangeLogicalPlan";
    }

    public LogicalPlanRequest getRequest()
    {
      return request;
    }

    public void setRequest(LogicalPlanRequest request)
    {
      this.request = request;
    }

  }

  public static class OperatorErrorEvent extends PhysicalOperatorEvent
  {
    private String containerId;
    private String errorMessage;
    private long failureId;

    public OperatorErrorEvent(String operatorName, int operatorId, String containerId, String errorMessage, long failureId)
    {
      this(operatorName, operatorId, containerId, errorMessage, LogLevel.ERROR, failureId);
    }

    public OperatorErrorEvent(String operatorName, int operatorId, String containerId, String errorMessage, LogLevel logLevel, long failureId)
    {
      super(operatorName, operatorId, logLevel);
      this.containerId = containerId;
      this.errorMessage = errorMessage;
      this.failureId = failureId;
    }

    @Override
    public String getType()
    {
      return "OperatorError";
    }

    public String getContainerId()
    {
      return containerId;
    }

    public void setContainerId(String containerId)
    {
      this.containerId = containerId;
    }

    public String getErrorMessage()
    {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
      this.errorMessage = errorMessage;
    }

    public long getFailureId()
    {
      return failureId;
    }

    public void setFailureId(long failureId)
    {
      this.failureId = failureId;
    }
  }

  public static class ContainerErrorEvent extends StramEvent
  {
    private String containerId;
    private String errorMessage;

    public ContainerErrorEvent(String containerId, String errorMessage)
    {
      this(containerId, errorMessage, LogLevel.ERROR);
    }

    public ContainerErrorEvent(String containerId, String errorMessage, LogLevel logLevel)
    {
      super(logLevel);
      this.containerId = containerId;
      this.errorMessage = errorMessage;
    }

    @Override
    public String getType()
    {
      return "ContainerError";
    }

    public String getContainerId()
    {
      return containerId;
    }

    public void setContainerId(String containerId)
    {
      this.containerId = containerId;
    }

    public String getErrorMessage()
    {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
      this.errorMessage = errorMessage;
    }

  }

}
