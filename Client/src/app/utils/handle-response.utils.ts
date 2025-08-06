import { LoggingService } from '../service/logging.service';

export function handleResposne<T>(
  loggingService: LoggingService,
  onSuccess: (response: T) => void,
  finallyFn?: () => void
) {
  return {
    next: (response: T) => {
      onSuccess(response);
      finallyFn?.();
    },
    error: (error: string) => {
      loggingService.onError(error);
      finallyFn?.();
    },
  };
}
