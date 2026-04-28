import jwt
import functools
from django.conf import settings
from django.http import JsonResponse


def _get_token_payload(request):
    auth_header = request.headers.get('Authorization', '')
    if not auth_header.startswith('Bearer '):
        return None, JsonResponse({'error': 'No auth token'}, status=401)
    token = auth_header.split(' ', 1)[1]
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=['HS256'])
        return payload, None
    except jwt.ExpiredSignatureError:
        return None, JsonResponse({'error': 'Token timeout'}, status=401)
    except jwt.InvalidTokenError:
        return None, JsonResponse({'error': 'Wrong token'}, status=401)


def require_auth(view_func):
    @functools.wraps(view_func)
    def wrapper(request, *args, **kwargs):
        payload, error = _get_token_payload(request)
        if error:
            return error
        request.jwt_payload = payload
        return view_func(request, *args, **kwargs)
    return wrapper


def require_admin(view_func):
    @functools.wraps(view_func)
    def wrapper(request, *args, **kwargs):
        payload, error = _get_token_payload(request)
        if error:
            return error
        if payload.get('role') != 'ADMIN':
            return JsonResponse({'error': 'Insufficient permissions.'}, status=403)
        request.jwt_payload = payload
        return view_func(request, *args, **kwargs)
    return wrapper
