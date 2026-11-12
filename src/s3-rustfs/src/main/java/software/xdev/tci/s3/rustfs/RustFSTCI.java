/*
 * Copyright © 2024 XDEV Software (https://xdev.software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.xdev.tci.s3.rustfs;

import java.net.URI;
import java.util.List;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.xdev.tci.TCI;
import software.xdev.tci.s3.rustfs.containers.RustFSContainer;


public class RustFSTCI extends TCI<RustFSContainer>
{
	private final List<String> initialBucketsToCreate;
	
	private S3Client s3Client;
	private S3Presigner s3Presigner;
	
	public RustFSTCI(
		final RustFSContainer container,
		final String networkAlias,
		final List<String> initialBucketsToCreate)
	{
		super(container, networkAlias);
		this.initialBucketsToCreate = initialBucketsToCreate;
	}
	
	public S3Client s3Client()
	{
		if(this.s3Client == null)
		{
			this.initS3Client();
		}
		
		return this.s3Client;
	}
	
	protected synchronized void initS3Client()
	{
		if(this.s3Client == null)
		{
			this.s3Client = this.createS3Client();
		}
	}
	
	protected S3Client createS3Client()
	{
		return S3Client.builder()
			.credentialsProvider(this.createCredentialsProvider())
			.region(Region.US_EAST_1) // Just here so that it doesn't crash
			.endpointOverride(URI.create(this.getContainer().gets3Url()))
			.forcePathStyle(true)
			.build();
	}
	
	public S3Presigner s3Presigner()
	{
		if(this.s3Presigner == null)
		{
			this.initS3Presigner();
		}
		return this.s3Presigner;
	}
	
	protected synchronized void initS3Presigner()
	{
		if(this.s3Presigner == null)
		{
			this.s3Presigner = this.createS3Presigner();
		}
	}
	
	protected S3Presigner createS3Presigner()
	{
		return S3Presigner.builder()
			.credentialsProvider(this.createCredentialsProvider())
			.region(Region.US_EAST_1) // Just here so that it doesn't crash
			.endpointOverride(URI.create(this.getExternalS3Url()))
			.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
			.build();
	}
	
	protected AwsCredentialsProvider createCredentialsProvider()
	{
		return StaticCredentialsProvider.create(AwsBasicCredentials.create(
			this.getContainer().getUsername(),
			this.getContainer().getPassword()));
	}
	
	@SuppressWarnings("resource")
	public RustFSTCI createBucket(final String bucket)
	{
		try
		{
			this.s3Client().createBucket(b -> b.bucket(bucket));
		}
		catch(final Exception e)
		{
			throw new IllegalStateException("Failed to create bucket " + bucket, e);
		}
		return this;
	}
	
	@Override
	public void start(final String containerName)
	{
		super.start(containerName);
		
		this.initialBucketsToCreate.forEach(this::createBucket);
	}
	
	@Override
	public void stop()
	{
		this.closeAndFree(this.s3Client, "s3Client", () -> this.s3Client = null);
		this.closeAndFree(this.s3Presigner, "s3Presigner", () -> this.s3Presigner = null);
		super.stop();
	}
	
	public String getExternalS3Url()
	{
		return this.getContainer().gets3Url();
	}
	
	public static String getInternalS3URL(final String dnsName)
	{
		// noinspection HttpUrlsUsage
		return "http://" + dnsName + ":" + RustFSContainer.S3_PORT;
	}
}
