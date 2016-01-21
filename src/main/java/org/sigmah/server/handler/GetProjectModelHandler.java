package org.sigmah.server.handler;

/*
 * #%L
 * Sigmah
 * %%
 * Copyright (C) 2010 - 2016 URD
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.sigmah.server.dao.ProjectModelDAO;
import org.sigmah.server.dispatch.impl.UserDispatch.UserExecutionContext;
import org.sigmah.server.domain.ProjectModel;
import org.sigmah.server.handler.base.AbstractCommandHandler;
import org.sigmah.shared.command.GetProjectModel;
import org.sigmah.shared.dispatch.CommandException;
import org.sigmah.shared.dto.ProjectModelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Handler for {@link GetProjectModel} command.
 * 
 * @author Maxime Lombard (mlombard@ideia.fr) (v2.0)
 * @author Denis Colliot (dcolliot@ideia.fr) (v2.0)
 */
public class GetProjectModelHandler extends AbstractCommandHandler<GetProjectModel, ProjectModelDTO> {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(GetProjectModelHandler.class);

	/**
	 * Injected {@link ProjectModelDAO}.
	 */
	@Inject
	private ProjectModelDAO projectModelDAO;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProjectModelDTO execute(final GetProjectModel cmd, final UserExecutionContext context) throws CommandException {

		final Integer modelId = cmd.getModelId();
		LOG.debug("Retrieving project model with id '{}'.", modelId);

		final ProjectModel model = projectModelDAO.findById(modelId);

		if (model == null) {
			LOG.debug("Project model with id #{} does not exist.", modelId);
			return null;
		}

		LOG.debug("Found project model with id #{}.", modelId);

		return mapper().map(model, new ProjectModelDTO(), cmd.getMappingMode());
	}

}
