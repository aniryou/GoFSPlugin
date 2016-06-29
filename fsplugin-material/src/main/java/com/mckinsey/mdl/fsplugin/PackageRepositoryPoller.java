/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/
package com.mckinsey.mdl.fsplugin;

import com.mckinsey.mdl.fsplugin.message.CheckConnectionResultMessage;
import com.mckinsey.mdl.fsplugin.message.PackageMaterialProperties;
import com.mckinsey.mdl.fsplugin.message.PackageMaterialProperty;
import com.mckinsey.mdl.fsplugin.message.PackageRevisionMessage;
import com.mckinsey.mdl.fsplugin.message.ValidationError;

import static com.mckinsey.mdl.fsplugin.message.CheckConnectionResultMessage.STATUS.*;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Date;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException; 
import javax.xml.bind.DatatypeConverter;

public class PackageRepositoryPoller {

    private PackageRepositoryConfigurationProvider configurationProvider;

    public PackageRepositoryPoller(PackageRepositoryConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public CheckConnectionResultMessage checkConnectionToRepository(PackageMaterialProperties repositoryConfiguration) {
        // check repository connection here
        PackageMaterialProperty pDir = repositoryConfiguration.getProperty(Constants.DIR);
        String dir = pDir.value();
        File f = new File(dir);
        if(!(f.exists() && f.isDirectory()))
        	return new CheckConnectionResultMessage(FAILURE, asList(dir + " not accessible or is not a directory"));
        return new CheckConnectionResultMessage(SUCCESS, asList("success message"));
    }

    public CheckConnectionResultMessage checkConnectionToPackage(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        // check package connection here
        PackageMaterialProperty pDir = repositoryConfiguration.getProperty(Constants.DIR);
        PackageMaterialProperty pFileName = packageConfiguration.getProperty(Constants.FILE_NAME);
        String dir = pDir.value();
        String fileName = pFileName.value();
        File f = new File(dir, fileName);
        if(!(f.exists() && f.isFile()))
        	return new CheckConnectionResultMessage(FAILURE, asList(fileName + " not accessible or is not a file"));
        return new CheckConnectionResultMessage(SUCCESS, asList("success message"));
    }

    public PackageRevisionMessage getLatestRevision(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        // get latest modification here
        PackageMaterialProperty pDir = repositoryConfiguration.getProperty(Constants.DIR);
        PackageMaterialProperty pFileName = packageConfiguration.getProperty(Constants.FILE_NAME);
        String dir = pDir.value();
        String fileName = pFileName.value();
        File f = new File(dir, fileName);
        if(!(f.exists() && f.isFile()))
        	return null;
        String revision = null;
		try {
			revision = getFileHash(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Date ts = new Date(f.lastModified());
    	String user = "user";
    	String revisionComment = "revisionComment";
    	String trackbackUrl = "http://null";
        return new PackageRevisionMessage(revision, ts, user, revisionComment, trackbackUrl);
    }

    public PackageRevisionMessage getLatestRevisionSince(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration, PackageRevisionMessage previousPackageRevision) {
        // get latest modification since here
        PackageRevisionMessage prm = getLatestRevision(packageConfiguration, repositoryConfiguration);
        if (prm.getTimestamp().after(previousPackageRevision.getTimestamp())) {
            return prm;
        }
        return null;
    }

    public String getFileHash(File file) throws IOException { 
    	MessageDigest md; 
    	try { 
    		md = MessageDigest.getInstance("MD5"); 
    	} catch (NoSuchAlgorithmException e) { 
    		throw new RuntimeException(e); 
    	} 
    	InputStream is = new FileInputStream(file); 
    	byte[] buf = new byte[1024]; 
    	try { 
    		is = new DigestInputStream(is, md); 
    		// read stream to EOF as normal... 
    		while(is.read(buf) > 0); 
    	} 
    	finally { 
    		is.close(); 
    	} 
    	byte[] digest = md.digest(); 
    	return DatatypeConverter.printBase64Binary(digest); 
    } 
}
