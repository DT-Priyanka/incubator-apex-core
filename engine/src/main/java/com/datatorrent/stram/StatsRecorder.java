/**
 * Copyright (C) 2015 DataTorrent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.stram;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.datatorrent.stram.webapp.OperatorInfo;

/**
 * <p>StatsRecorder interface.</p>
 *
 * @since 0.3.4
 */
public interface StatsRecorder
{
  public void recordContainers(Map<String, StreamingContainerAgent> containerMap, long timestamp) throws IOException;

  public void recordOperators(List<OperatorInfo> operatorList, long timestamp) throws IOException;

}
