import { HttpClient, HttpDownloadProgressEvent, HttpEvent, HttpEventType, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subscription } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SseClient {

    readonly DEFAULT_OPTIONS: any = {
        observe: 'events',
        reportProgress: true,
        responseType: 'text',
    };

    constructor(private httpClient: HttpClient) { }

    public stream(url: string, body: string): Observable<string> {
        return new Observable<string>((observer) => {
            let processedChunks = 0;

            const streamSubscription = (): Subscription => {
                return this.httpClient
                    .request<string>('POST', url, { ...this.DEFAULT_OPTIONS, body })
                    .subscribe(parseStreamEvent);
            };

            const parseStreamEvent = (event: HttpEvent<string>): void => {
                if (event.type === HttpEventType.DownloadProgress) {
                    const { partialText } = event as HttpDownloadProgressEvent;
                    if (partialText) {
                        onProgress(partialText);
                    }
                } else if (event.type === HttpEventType.Response) {
                    onComplete(event as HttpResponse<string>);
                }
            };

            const parseData = (inputString: string): string[] => inputString.split('\n\n')
                .map(line => /^data:(?<data>.*)$/.exec(line)?.groups)
                .filter(groups => !!groups)
                .map(groups => groups['data']);

            const onProgress = (data: string): void => {
                const allChunks = parseData(data);
                const unprocessedChunks = allChunks.slice(processedChunks);
                unprocessedChunks.forEach(chunk => observer.next(chunk));
                processedChunks = allChunks.length;
            };

            const onComplete = (response: HttpResponse<string>): void => {
                const finalResponse = response.body;
                if (finalResponse) {
                    onProgress(finalResponse);
                }
                observer.complete();
            };

            const sub = streamSubscription();
            return () => sub.unsubscribe();
        });
    }
}