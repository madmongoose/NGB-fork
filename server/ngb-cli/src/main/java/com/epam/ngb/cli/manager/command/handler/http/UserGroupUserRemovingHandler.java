/*
 * MIT License
 *
 * Copyright (c) 2018 EPAM Systems
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epam.ngb.cli.manager.command.handler.http;

import com.epam.ngb.cli.app.ApplicationOptions;
import com.epam.ngb.cli.constants.MessageConstants;
import com.epam.ngb.cli.entity.ExtendedRole;
import com.epam.ngb.cli.entity.NgbUser;
import com.epam.ngb.cli.entity.Role;
import com.epam.ngb.cli.manager.command.handler.Command;
import com.epam.ngb.cli.manager.printer.AbstractResultPrinter;
import com.epam.ngb.cli.manager.request.RequestManager;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.client.methods.HttpDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ngb.cli.constants.MessageConstants.ERROR_ROLE_MODEL_IS_NOT_SUPPORTED;
import static com.epam.ngb.cli.constants.MessageConstants.ILLEGAL_COMMAND_ARGUMENTS;

/**
 * Deletes specific users from a existing user group
 */
@Command(type = Command.Type.REQUEST, command = {"rem_group"})
public class UserGroupUserRemovingHandler extends AbstractHTTPCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupUserRemovingHandler.class);

    /**
     * If true command will output in a table format, otherwise
     * json format will be used
     */
    private boolean printTable;

    /**
     * If true command will output result in a json format
     */
    private boolean printJson;

    private List<Long> users = Collections.emptyList();
    private Long groupId;

    /**
     * Verifies input arguments
     * @param arguments command line arguments for 'grant_permission' command
     * @param options
     */
    @Override
    public void parseAndVerifyArguments(List<String> arguments, ApplicationOptions options) {

        printTable = options.isPrintTable();
        printJson = options.isPrintJson();

        if (arguments.size() != 1) {
            throw new IllegalArgumentException(MessageConstants.getMessage(
                    ILLEGAL_COMMAND_ARGUMENTS, getCommand(), 1, arguments.size()));
        }

        if (!isRoleModelEnable()) {
            throw new IllegalArgumentException(MessageConstants.getMessage(
                    ERROR_ROLE_MODEL_IS_NOT_SUPPORTED));
        }

        if (options.getUsers() == null) {
            throw new IllegalArgumentException("At least one user should be provided! Use -u (--users) option");
        }

        List<String> namesOrIds = Arrays.stream(options.getUsers().split(",")).collect(Collectors.toList());
        if (namesOrIds.stream().allMatch(NumberUtils::isDigits)) {
            users = namesOrIds.stream().map(Long::parseLong).collect(Collectors.toList());
        } else {
            List<NgbUser> loaded = loadListOfUsers(namesOrIds);
            if (loaded.size() != namesOrIds.size()) {
                Set<String> namesSet = loaded.stream().map(NgbUser::getUserName).collect(Collectors.toSet());
                namesOrIds.forEach(name -> {
                    if (!namesSet.contains(name)) {
                        LOGGER.error("User with name: " + name + " not found!");
                    }
                });
            }
            users = loaded.stream().map(NgbUser::getId).collect(Collectors.toList());
        }

        String roleIdentifier = arguments.get(0);
        if (NumberUtils.isDigits(roleIdentifier)) {
            this.groupId = Long.parseLong(roleIdentifier);
        } else {
            Role role = loadRoleByName(roleIdentifier);
            if (role == null) {
                throw new IllegalArgumentException("Group with name: '" + roleIdentifier + "' not found!");
            }
            this.groupId = role.getId();
        }
    }

    @Override public int runCommand() {
        HttpDelete delete = (HttpDelete) getRequestFromURLByType("DELETE",
                getServerParameters().getServerUrl()
                        + String.format(getRequestUrl(), groupId,
                users.stream().map(String::valueOf).collect(Collectors.joining(","))));
        ExtendedRole result = getResult(RequestManager.executeRequest(delete), ExtendedRole.class);

        if (printJson || printTable) {
            AbstractResultPrinter printer = AbstractResultPrinter
                    .getPrinter(printTable, result.getUsers().get(0).getFormatString(result.getUsers()));
            printer.printHeader(result.getUsers().get(0));
            result.getUsers().forEach(printer::printItem);
        }
        return 0;
    }

}
