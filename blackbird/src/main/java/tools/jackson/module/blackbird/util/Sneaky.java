/*
 * Copyright (c), Data Geekery GmbH, contact@datageekery.com
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
package tools.jackson.module.blackbird.util;

import java.lang.reflect.InvocationTargetException;

public class Sneaky {
    private Sneaky() {
        throw new RuntimeException();
    }

    /**
     * Will <b>always</b> throw an exception, so the caller should also always throw the dummy return value to make sure the control flow remains clear.
     */
    public static RuntimeException throwAnyway(Throwable t) {
        if (t instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }

        if (t instanceof InvocationTargetException) {
            throw throwAnyway(t.getCause());
        }

        throwEvadingChecks(t);

        // never reached
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwEvadingChecks(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
