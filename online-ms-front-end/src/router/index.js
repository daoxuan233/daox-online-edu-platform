import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 公共页面
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/auth/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/auth/Register.vue')
    },
    {
      path: '/',
      name: 'PublicHome',
      component: () => import('../views/Home.vue')
    },
    // 学生端页面 (嵌套在 /student 布局下)
    {
      path: '/index',
      component: () => import('../layouts/StudentLayout.vue'),
      children: [
        {
          path: '',
          name: 'StudentHome',
          component: () => import('../views/student/Home.vue')
        },
        {
          path: 'courses',
          name: 'Courses',
          component: () => import('../views/student/Courses.vue')
        },
        {
          path: 'courses/:id',
          name: 'CourseDetail',
          component: () => import('../views/student/CourseDetail.vue')
        },
        {
          path: 'my-courses',
          name: 'MyCourses',
          component: () => import('../views/student/MyCourses.vue')
        },
        {
          path: 'learn/:courseId',
          name: 'Learn',
          component: () => import('../views/student/Learn.vue')
        },
        {
          path: 'assessments',
          name: 'Assessments',
          component: () => import('../views/student/Assessments.vue')
        },
        {
          path: 'exam/:assessmentId',
          name: 'Exam',
          component: () => import('../views/student/Exam.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/student/Profile.vue')
        },
        {
          path: 'notes',
          name: 'Notes',
          component: () => import('../views/student/Notes.vue')
        },
        {
          path: 'chat',
          name: 'StudentChat',
          component: () => import('../views/student/StuChat.vue')
        }
      ]
    },
    
    // 教师端页面 (嵌套在 /teacher 布局下)
    {
      path: '/teacher',
      component: () => import('../layouts/TeacherLayout.vue'),
      children: [
        {
          path: '',
          name: 'TeacherDashboard',
          component: () => import('../views/teacher/TeacherDashboard.vue')
        },
        {
          path: 'courses',
          name: 'TeacherCourses',
          component: () => import('../views/teacher/TeacherCourses.vue')
        },
        {
          path: 'chat',
          name: 'TeacherChat',
          component: () => import('../views/teacher/TeacherChat.vue')
        },
        {
          path: 'courses/new',
          name: 'NewCourse',
          component: () => import('../views/teacher/NewCourse.vue')
        },
        {
          path: 'courses/:id/edit',
          name: 'CourseEdit',
          component: () => import('../views/teacher/CourseEdit.vue')
        },
        {
          path: 'course-outline',
          name: 'CourseOutline',
          component: () => import('../views/teacher/CourseOutline.vue')
        },
        {
          path: 'questions',
          name: 'QuestionBank',
          component: () => import('../views/teacher/QuestionBank.vue')
        },
        {
          path: 'assessments',
          name: 'AssessmentManagement',
          component: () => import('../views/teacher/AssessmentManagement.vue')
        },
        {
          path: 'paper-center',
          name: 'PaperCenter',
          component: () => import('../views/teacher/PaperCenter.vue')
        },
        {
          path: 'grading',
          name: 'GradingCenter',
          component: () => import('../views/teacher/GradingCenter.vue')
        },
        {
          path: 'grading/:assessmentId',
          name: 'GradingDetail',
          component: () => import('../views/teacher/GradingDetail.vue')
        },
        {
          path: 'grading/:assessmentId/:questionId',
          name: 'ImmersiveGrading',
          component: () => import('../views/teacher/ImmersiveGrading.vue')
        },
        {
          path: 'profile',
          name: 'TeacherProfile',
          component: () => import('../views/teacher/TeacherProfile.vue')
        },
        {
          path: 'settings',
          name: 'TeacherSettings',
          component: () => import('../views/teacher/TeacherSettings.vue')
        },
        {
          path: 'analytics',
          name: 'TeacherAnalytics',
          component: () => import('../views/teacher/TeacherAnalytics.vue')
        },
      ]
    },
    
    // 管理员页面 (嵌套在 /admin 布局下)
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      children: [
        {
          path: '',
          name: 'AdminDashboard',
          component: () => import('../views/admin/AdminDashboard.vue')
        },
        {
          path: 'users',
          name: 'UserManagement',
          component: () => import('../views/admin/UserManagement.vue')
        },
        {
          path: 'categories',
          name: 'CategoryManagement',
          component: () => import('../views/admin/CategoryManagement.vue')
        }
      ]
    }
  ]
})

export default router
