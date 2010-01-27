package org.activityinfo.server.endpoint.gwtrpc;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.activityinfo.server.dao.AuthenticationDAO;
import org.activityinfo.server.dao.Transactional;
import org.activityinfo.server.domain.Authentication;
import org.activityinfo.server.domain.DomainFilters;
import org.activityinfo.server.endpoint.gwtrpc.handler.CommandHandler;
import org.activityinfo.server.endpoint.gwtrpc.handler.HandlerUtil;
import org.activityinfo.shared.command.Command;
import org.activityinfo.shared.command.RemoteCommandService;
import org.activityinfo.shared.command.result.CommandResult;
import org.activityinfo.shared.exception.CommandException;
import org.activityinfo.shared.exception.InvalidAuthTokenException;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;


/**
 * Process command objects from the client and returns CommandResults.
 * <p/>
 * This servlet is at the heart of the command execution pipeline, but delegates all
 * logic processing to the {@link org.activityinfo.server.endpoint.gwtrpc.handler.CommandHandler} corresponding
 * to the given {@link org.activityinfo.shared.command.Command}s.
 * <p/>
 * CommandHandlers are loaded based on name from the org.activityinfo.server.command.handler package.
 * <p/>
 * E.g. UpdateEntity => org.activityinfo.server.command.handler.UpdateEntityHandler
 */
@Singleton
public class CommandServlet extends RemoteServiceServlet implements RemoteCommandService {

    @Inject
    private Injector injector;

    /**
     * Overrides the default implementation to intercept exceptions (primarily serialization
     * exceptions at this point) and log them.
     *
     * @param arg0
     * @return
     * @throws SerializationException
     */
    @Override
    public String processCall(String arg0) throws SerializationException {
        String result;
        try {
            result = super.processCall(arg0);
        } catch (SerializationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return result;
    }


    @Override
    public List<CommandResult> execute(String authToken, List<Command> commands) throws CommandException {

        EntityManager em = injector.getInstance(EntityManager.class);

        Authentication session = retrieveAuthentication(authToken);

        try {

            DomainFilters.applyUserFilter(session.getUser(), em);

            return handleCommands(commands, session);

        } catch (Throwable caught) {
            caught.printStackTrace();
            throw new CommandException();
        }
    }

    private List<CommandResult> handleCommands(List<Command> commands, Authentication auth) {
        List<CommandResult> results = new ArrayList<CommandResult>();
        for (Command command : commands) {
            try {
                results.add(handleCommand(auth, command));
            } catch (CommandException e) {
                // continue executing other commands in the call
            }
        }
        return results;
    }

    @Transactional
    private CommandResult handleCommand(Authentication auth, Command command) throws CommandException {
        CommandHandler handler = createHandler(command);
        return handler.execute(command, auth.getUser());
    }

    private CommandHandler createHandler(Command command) {
        return (CommandHandler) injector.getInstance(
                HandlerUtil.executorForCommand(command));
    }

    private Authentication retrieveAuthentication(String authToken) throws InvalidAuthTokenException {
        AuthenticationDAO authDAO = injector.getInstance(AuthenticationDAO.class);
        Authentication auth = authDAO.findById(authToken);
        if (auth == null) {
            throw new InvalidAuthTokenException();
        }
        return auth;
    }
}