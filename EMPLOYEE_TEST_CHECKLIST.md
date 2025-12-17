# Employee Module Manual Test Checklist

Follow these steps in the running application (default port 8997).

1. Admin: Open Admin Employees page
   - URL: /admin/employees
   - Expect: list (empty or existing), + "Ajouter un Employé" button, stats cards

2. Create Employee (admin)
   - Click "Ajouter un Employé" → fill First name, Last name, Email, Phone → Submit
   - Expect: success flash with default credentials `temp123` and employee visible in list

3. First-login flow (employee)
   - Login as the new employee using the provided email and password `temp123` (employee login page)
   - Expect: automatic redirect to `/employee/change-password`
   - Fill new password (>= 6 chars) and confirm → submit
   - Expect: success message and redirect to `/employee-login?logout=true` (forced logout)
   - Re-login with new password → access `/employee/dashboard`

4. Time tracking (employee)
   - From dashboard or `/employee/time-tracking` click "Enregistrer Arrivée"
   - Expect: success flash, arrival time recorded (visible in today's row)
   - Click "Enregistrer Départ" → Expect departure recorded; error if arrival missing

5. Absence declaration (employee)
   - Go to `/employee/absences` → create an absence with date, type, reason
   - Expect: success flash and new row in history

6. Observations (employee)
   - Go to `/employee/observations` → submit a new observation (title, description, priority)
   - Expect: success flash and entry in list

7. Admin: Employee profile and histories
   - Open `/admin/employees/{id}/profile` → Expect identity section, recent time-tracking (10), absences (10), observations (5)
   - Open `/admin/employees/{id}/time-tracking`, `/absences`, `/observations` and verify full lists

8. Edit & Delete (admin)
   - Edit employee: /admin/employees/{id}/edit → change phone or enabled status → save → expect success
   - Delete employee: click delete → confirm → expect employee removed and success flash

9. Security checks
   - Verify `/api/debug/admin-status` and `/api/debug/reset-admin` require ADMIN (authenticated as admin)
   - Try accessing `/api/debug/*` as non-admin → expect access denied

Notes:
- If you see an issue with the `first_login` column at startup, check logs; the app includes a runtime schema runner that attempts to add it automatically.
- Remove or restrict debug endpoints before production (they are now limited to ADMIN role in `SecurityConfig`).

If any step fails, copy the relevant server log lines and the URL used and paste them here for debugging.
