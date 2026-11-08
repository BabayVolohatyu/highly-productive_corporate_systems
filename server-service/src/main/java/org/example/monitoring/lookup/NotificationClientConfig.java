package org.example.monitoring.lookup;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.contracts.notification.NotificationLookupGrpc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(NotificationClientProperties.class)
public class NotificationClientConfig {

    public NotificationClientConfig(NotificationClientProperties properties) {
        ClientMode.requireKnown(properties.mode());
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.HTTP)
    RestClient notificationRestClient(NotificationClientProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.http().connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.http().readTimeout());
        return RestClient.builder()
                .baseUrl(properties.http().baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.HTTP)
    NotificationHttpApi notificationHttpApi(RestClient notificationRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(notificationRestClient);
        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(NotificationHttpApi.class);
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.HTTP)
    AlertStatusLookup httpAlertStatusLookup(NotificationHttpApi api) {
        return new HttpAlertStatusLookup(api);
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.HTTP)
    AlertCountLookup httpAlertCountLookup(NotificationHttpApi api) {
        return new HttpAlertCountLookup(api);
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.HTTP)
    DeliveryReceiptLookup httpDeliveryReceiptLookup(NotificationHttpApi api) {
        return new HttpDeliveryReceiptLookup(api);
    }

    @Bean(destroyMethod = "shutdownNow")
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.GRPC)
    ManagedChannel notificationChannel(NotificationClientProperties properties) {
        return ManagedChannelBuilder.forTarget(properties.grpc().address()).usePlaintext().build();
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.GRPC)
    NotificationLookupGrpc.NotificationLookupBlockingStub notificationStub(ManagedChannel notificationChannel) {
        return NotificationLookupGrpc.newBlockingStub(notificationChannel);
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.GRPC)
    AlertStatusLookup grpcAlertStatusLookup(NotificationLookupGrpc.NotificationLookupBlockingStub stub,
                                             NotificationClientProperties properties) {
        return new GrpcAlertStatusLookup(stub, properties.grpc().deadline());
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.GRPC)
    AlertCountLookup grpcAlertCountLookup(NotificationLookupGrpc.NotificationLookupBlockingStub stub,
                                           NotificationClientProperties properties) {
        return new GrpcAlertCountLookup(stub, properties.grpc().deadline());
    }

    @Bean
    @ConditionalOnProperty(name = "notification.client.mode", havingValue = ClientMode.GRPC)
    DeliveryReceiptLookup grpcDeliveryReceiptLookup(NotificationLookupGrpc.NotificationLookupBlockingStub stub,
                                                     NotificationClientProperties properties) {
        return new GrpcDeliveryReceiptLookup(stub, properties.grpc().deadline());
    }
}
