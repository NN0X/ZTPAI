from django.http import JsonResponse, Http404

class ValidationError(Exception):
    def __init__(self, errors):
        self.errors = errors


class ErrorHandlerMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        response = self.get_response(request)
        return response

    def process_exception(self, request, exception):
        if isinstance(exception, ValidationError):
            return JsonResponse({
                'status': 400,
                'error': 'Błąd walidacji',
                'messages': exception.errors
            }, status=400)

        if isinstance(exception, Http404):
            return JsonResponse({
                'status': 404,
                'error': 'Nie znaleziono zasobu',
            }, status=404)

        return JsonResponse({
            'status': 500,
            'error': 'Wewnętrzny błąd serwera',
            'message': str(exception)
        }, status=500)
