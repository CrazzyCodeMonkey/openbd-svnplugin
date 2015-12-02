/* 
 *  Copyright (C) 2000 - 2015 aw2.0Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *  
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *  
 *  Additional permission under GNU GPL version 3 section 7
 *  
 *  If you modify this Program, or any covered work, by linking or combining 
 *  it with any of the JARS listed in the README.txt (or a modified version of 
 *  (that library), containing parts covered by the terms of that JAR, the 
 *  licensors of this Program grant you additional permission to convey the 
 *  resulting work. 
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *  
 *  http://openbd.org/
 */
package net.aw20.openbd.plugins.svn.functions;


/** 
 * OpenBD class for function: SVNUpdate()
 * 
 * Called from OpenBD <code>SVNUpdate("mySVN","temp/", "C:\temp\",-1,true,true)</code>
 * 
 * @author Trace Sinclair
 * @version 2.0.0
 * @since 2.0.0
 * @see http://svnkit.com
 */
import java.util.Map;

import org.tmatesoft.svn.core.SVNException;

import net.aw20.openbd.plugins.svn.SVNRepo;
import net.aw20.openbd.plugins.svn.functions.SVNGetDir;
import net.aw20.openbd.plugins.svn.functions.SVNGetFile;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class SVNUpdate extends functionBase {

	private static final long serialVersionUID = 1L;


	public SVNUpdate() {
		min = 3;
		max = 6;
		setNamedParams( new String[] {
				"name",
				"svnPath",
				"localPath",
				"revision",
				"recursive",
				"properties"
		} );
	}


	@Override
	public String[] getParamInfo() {
		return new String[] {
				"Name of the SVN Repository to use",
				"SVN Path to get",
				"Full local path where to get to",
				"The revision to get, default HEAD",
				"Flag to indicate if subdirectories are to be retrieved as well, default to false",
				"Flag to indicate if the properties is to be returned, default to false"
		};
	}


	@Override
	public Map<String, String> getInfo() {
		return makeInfo(
				"SVN",
				"Get files and directories from the given repository at a revision.",
				ReturnType.BOOLEAN );
	}


	/**
	 * Alternate function to call SVNGetFile or SVNGetDir without having to know what type
	 * 
	 * Called from OpenBD <code>SVNUpdate("mySVN","temp/", "C:\temp\",-1,true,true)</code>
	 * 
	 * @param _session
	 * @param _argStruct
	 *          name: String repository name to use
	 *          svnPath: String path in SVN to get
	 *          localPath: String path on the local file system to get files to
	 *          revision: Int revision to get
	 *          recursive: boolean flag to indicate if we get all subdirectories (true: yes / false: no), default to false
	 *          properties: boolean flat to indicate if we expect a struct of properties are returned (true: yes / false: no), default to false
	 * @return if (properties) a struct modeled after the file structure retrieve, file elements will have SVN property values, directories will have structs
	 * @return if (!properties) true
	 * @see net.aw20.openbd.plugins.svn.functions.SVNGetDir
	 * @see net.aw20.openbd.plugins.svn.functions.SVNGetFile
	 * @see net.aw20.openbd.plugins.svn.SVNRepo
	 * @since 2.0.0
	 */
	@Override
	public cfData execute( cfSession _session, cfArgStructData _argStruct ) throws cfmRunTimeException {

		// Get all arguments
		String name = getNamedStringParam( _argStruct, "name", "" ).trim();
		String svnPath = getNamedStringParam( _argStruct, "svnPath", "" ).trim();
		String localPath = getNamedStringParam( _argStruct, "localPath", "" ).trim();
		int revision = getNamedIntParam( _argStruct, "revision", SVNRepo.HEAD );
		boolean recursive = getNamedParam( _argStruct, "recursive", cfBooleanData.FALSE ).getBoolean();
		boolean properties = getNamedParam( _argStruct, "properties", cfBooleanData.FALSE ).getBoolean();

		String type = null;

		// Validate arguments
		if ( name.isEmpty() ) {
			throwException( _session, "Please provide a SVN Repository" );
		}

		try ( SVNRepo repo = new SVNRepo( name ) ) {
			type = repo.getPathType( svnPath, revision );

			// svnPath can be empty, it will grab all the contents of the repository
			if ( localPath.isEmpty() ) {
				throwException( _session, "Please provide a valid local direcot" );
			}

			// Call to do all the work
			if ( type.equals( "dir" ) ) {
				return new SVNGetDir().execute( _session, repo, svnPath, localPath, revision, recursive, properties );
			} else if ( type.equals( "file" ) ) {
				return new SVNGetFile().execute( _session, repo, svnPath, localPath, revision, properties );
			} else {
				return cfBooleanData.FALSE;
			}

		} catch ( SVNException e ) {
			throwException( _session, "Unable to connect to " + name + ". Please provide a Valid SVN Repository" );
		}

		return cfBooleanData.FALSE;

	}


}
