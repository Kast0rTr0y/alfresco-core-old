/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.ibatis;

/**
 * Interface for DAOs that offer batching.  This should be provided as an optimization
 * and DAO implementations that can't supply batching should just do nothing.
 *
 * @author Derek Hulley
 * @since 3.2.1
 */
public interface BatchingDAO
{
    /**
     * Start a batch of insert or update commands
     * 
     * @throws RuntimeException wrapping a SQLException
     */
    void startBatch();
    /**
     * Write a batch of insert or update commands
     * 
     * @throws RuntimeException wrapping a SQLException
     */
    void executeBatch();
}
