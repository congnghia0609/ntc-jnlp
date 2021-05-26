/*
 * Copyright 2021 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ntc.jvertx;

/**
 *
 * @author nghiatc
 * @since Jan 15, 2021
 */
public class CommonVX {
    public static final String LD_QUEUE = "ldqueue";
    public static final String SD_QUEUE = "sdqueue";
    public static final String VT_QUEUE = "vtqueue";
    
    // Error code
    public static final int SUCCESS = 0;
    public static final int NO_ACTION = -1;
    public static final int BAD_ACTION = -2;
    public static final int PARAMS_INVALID = -3;
    public static final int DB_ERROR = -4;
    public static final int NOT_EXIST = -5;
    public static final int NOT_PERMISSION = -6;
}
