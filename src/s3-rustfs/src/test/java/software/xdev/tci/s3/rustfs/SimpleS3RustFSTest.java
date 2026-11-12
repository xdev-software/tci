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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.xdev.tci.factory.registry.TCIFactoryRegistry;
import software.xdev.tci.s3.rustfs.factory.RustFSTCIFactory;


class SimpleS3RustFSTest
{
	protected static final String BUCKET = "test";
	protected static final RustFSTCIFactory FACTORY = new RustFSTCIFactory(BUCKET);
	
	protected RustFSTCI rustFSInfra;
	
	@BeforeAll
	static void beforeAll()
	{
		TCIFactoryRegistry.instance().warmUp();
	}
	
	@BeforeEach
	void beforeEach()
	{
		this.rustFSInfra = FACTORY.getNew(null);
	}
	
	@Test
	void check()
	{
		final S3Client s3Client = this.rustFSInfra.s3Client();
		final List<Bucket> buckets = s3Client.listBuckets().buckets();
		assertEquals(1, buckets.size());
		assertEquals(BUCKET, buckets.getFirst().name());
		
		final String fileName = "test-file.txt";
		s3Client.putObject(
			b -> b
				.bucket(BUCKET)
				.key(fileName),
			RequestBody.fromString("THIS_IS_A_TEST"));
		
		final List<S3Object> contents = s3Client.listObjects(b -> b.bucket(BUCKET))
			.contents();
		
		assertEquals(1, contents.size());
		assertEquals(fileName, contents.getFirst().key());
		
		final String presignedUrl = this.rustFSInfra.s3Presigner().presignGetObject(b -> b
				.getObjectRequest(br -> br
					.bucket(BUCKET)
					.key(fileName)
					.build())
				.signatureDuration(Duration.ofMinutes(10)))
			.url()
			.toString();
		
		Assertions.assertTrue(presignedUrl.startsWith(
			this.rustFSInfra.getExternalS3Url() + "/" + BUCKET + "/" + fileName));
	}
	
	@AfterEach
	void afterEach()
	{
		if(this.rustFSInfra != null)
		{
			this.rustFSInfra.stop();
			this.rustFSInfra = null;
		}
	}
	
	@AfterAll
	static void afterAll()
	{
		FACTORY.close();
	}
}
