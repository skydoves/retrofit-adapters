/*
 * Copyright (C) 2022 skydoves (Jaewoong Eum)
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skydoves.retrofit.adapters.core

import java.io.IOException

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.2.0
 *
 * Thrown when a http request succeeds but the response body is null, while the service method
 * declared a non-null body type.
 *
 * The call adapters report a null body as a successful value by default, which pushes the null far
 * away from the call site. Enable `nullBodyAsFailure` on the adapter factory to receive this
 * exception as a failure instead.
 */
public class NullBodyException(
  message: String,
) : IOException(message)
