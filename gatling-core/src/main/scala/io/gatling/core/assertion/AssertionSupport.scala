/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.core.assertion

import scala.reflect.io.Path

import io.gatling.core.result.{ GroupStatsPath, RequestStatsPath, StatsPath }
import io.gatling.core.result.message.Status
import io.gatling.core.result.reader.{ DataReader, GeneralStats }

trait AssertionSupport {

	def global = new Selector((reader, status) => reader.requestGeneralStats(None, None, status), "Global")

	def details(selector: Path) = {

		def path(reader: DataReader, selector: Path): Option[StatsPath] =
			if (selector.segments.isEmpty)
				None
			else {
				val selectedPath = selector.segments
				reader.statsPaths.find { statsPath =>
					val path = statsPath match {
						case RequestStatsPath(request, group) => group.map(_.hierarchy).getOrElse(Nil) :: List(request)
						case GroupStatsPath(group) => group.hierarchy
					}
					path == selectedPath
				}
			}

		def generalStats(selector: Path): (DataReader, Option[Status]) => GeneralStats = {
			(reader, status) =>
				path(reader, selector) match {
					case Some(RequestStatsPath(request, group)) => reader.requestGeneralStats(Some(request), group, status)
					case Some(GroupStatsPath(group)) => reader.requestGeneralStats(None, Some(group), status)
					case None => reader.requestGeneralStats(None, None, status)
				}
		}

		new Selector(generalStats(selector), selector.segments.mkString(" / "))
	}
}
