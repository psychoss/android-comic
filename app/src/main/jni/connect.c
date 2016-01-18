#include "connect.h"
#include <android/log.h>


#define TAG "Comic"
#define LOGW(...) (__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__))
#define LOGD(...) (__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))
#define LOGV(...) (__android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__))


int
init_connection(char *hostname, char *port, struct addrinfo **res) {
    LOGW("HOSTNAME", hostname);
    struct addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    return getaddrinfo(hostname, port, &hints, res);
}

int
make_connection(struct addrinfo *res) {
    int sockfd, status;

    sockfd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    jump_unless(sockfd > 0);

    status = connect(sockfd, res->ai_addr, res->ai_addrlen);
    jump_unless(status == 0);

    return sockfd;

    error:
    return -1;
}

char *
build_request(char *hostname, char *request_path) {
    char *request = NULL;
    Buffer *request_buffer = buffer_alloc(BUF_SIZE);

    buffer_appendf(request_buffer, "GET %s HTTP/1.0\r\n", request_path);
    buffer_appendf(request_buffer, "Host: %s\r\n", hostname);
    buffer_appendf(request_buffer, "Connection: close\r\n\r\n");

    request = buffer_to_s(request_buffer);
    buffer_free(request_buffer);

    return request;
}

int
make_request(int sockfd, char *hostname, char *request_path) {
    char *request = build_request(hostname, request_path);
    size_t bytes_sent = 0;
    size_t total_bytes_sent = 0;
    size_t bytes_to_send = strlen(request);


    while (1) {
        bytes_sent = send(sockfd, request, strlen(request), 0);
        total_bytes_sent += bytes_sent;


        if (total_bytes_sent >= bytes_to_send) {
            break;
        }
    }

    free(request);

    return total_bytes_sent;
}

int
fetch_response(int sockfd, Buffer **response, int recv_size) {
    size_t bytes_received;
    int status = 0;
    char data[recv_size];


    while (1) {
        bytes_received = recv(sockfd, data, RECV_SIZE, 0);

        if (bytes_received == -1) {
            return -1;
        } else if (bytes_received == 0) {
            return 0;
        }

        if (bytes_received > 0) {
            status = buffer_append(*response, data, bytes_received);
            if (status != 0) {
                fprintf(stderr, "Failed to append to buffer.\n");
                return -1;
            }
        }
    }


    return status;
}
