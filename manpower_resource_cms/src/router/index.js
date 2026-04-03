import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录', public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { title: '注册', public: true },
    },
    {
      path: '/',
      component: () => import('@/layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '工作台' },
        },
        // 员工管理
        {
          path: 'employee/list',
          name: 'employeeList',
          component: () => import('@/views/employee/EmployeeList.vue'),
          meta: { title: '员工列表' },
        },
        {
          path: 'employee/transfer',
          name: 'employeeTransfer',
          component: () => import('@/views/employee/TransferView.vue'),
          meta: { title: '员工异动' },
        },
        // 组织架构
        {
          path: 'org/department',
          name: 'department',
          component: () => import('@/views/org/DepartmentView.vue'),
          meta: { title: '部门管理' },
        },
        {
          path: 'org/position',
          name: 'position',
          component: () => import('@/views/org/PositionView.vue'),
          meta: { title: '岗位管理' },
        },
        // 考勤管理
        {
          path: 'attendance/clock',
          name: 'attendanceClock',
          component: () => import('@/views/attendance/ClockView.vue'),
          meta: { title: '我的打卡' },
        },
        {
          path: 'attendance/record',
          name: 'attendanceRecord',
          component: () => import('@/views/attendance/RecordView.vue'),
          meta: { title: '打卡记录' },
        },
        {
          path: 'attendance/location',
          name: 'attendanceLocation',
          component: () => import('@/views/attendance/LocationView.vue'),
          meta: { title: '打卡地点管理' },
        },
        {
          path: 'attendance/appeal',
          name: 'attendanceAppeal',
          component: () => import('@/views/attendance/MyAppealView.vue'),
          meta: { title: '我的申诉' },
        },
        {
          path: 'attendance/appeal-approve',
          name: 'attendanceAppealApprove',
          component: () => import('@/views/attendance/AppealApproveView.vue'),
          meta: { title: '申诉审批' },
        },
        {
          path: 'attendance/calendar',
          name: 'attendanceCalendar',
          component: () => import('@/views/attendance/AttendanceCalendarView.vue'),
          meta: { title: '考勤日历' },
        },
        {
          path: 'attendance/rule',
          name: 'attendanceRule',
          component: () => import('@/views/attendance/AttRuleView.vue'),
          meta: { title: '考勤规则' },
        },
        {
          path: 'attendance/daily-stat',
          name: 'attendanceDailyStat',
          component: () => import('@/views/attendance/AttDailyStatView.vue'),
          meta: { title: '日考勤统计' },
        },
        // 月度考勤统计（暂时隐藏）
        // {
        //   path: 'attendance/monthly-stat',
        //   name: 'attendanceMonthlyStat',
        //   component: () => import('@/views/attendance/AttMonthlyStatView.vue'),
        //   meta: { title: '月度考勤统计' },
        // },
        // 假期管理
        {
          path: 'leave/apply',
          name: 'leaveApply',
          component: () => import('@/views/leave/ApplyView.vue'),
          meta: { title: '请假申请' },
        },
        {
          path: 'leave/my',
          name: 'leaveList',
          component: () => import('@/views/leave/MyLeaveView.vue'),
          meta: { title: '我的假期' },
        },
        {
          path: 'leave/approve',
          name: 'leaveApprove',
          component: () => import('@/views/leave/ApproveView.vue'),
          meta: { title: '假期审批' },
        },
        {
          path: 'leave/type',
          name: 'leaveType',
          component: () => import('@/views/leave/LeaveTypeView.vue'),
          meta: { title: '假期类型管理' },
        },
        {
          path: 'leave/balance',
          name: 'leaveBalance',
          component: () => import('@/views/leave/LeaveBalanceView.vue'),
          meta: { title: '假期额度管理' },
        },
        // 薪资
        {
          path: 'salary/my',
          name: 'mySalary',
          component: () => import('@/views/salary/MySalaryView.vue'),
          meta: { title: '我的薪资' },
        },
        {
          path: 'salary/manage',
          name: 'salaryManage',
          component: () => import('@/views/salary/SalaryManageView.vue'),
          meta: { title: '薪资管理' },
        },
        {
          path: 'salary/item',
          name: 'salaryItem',
          component: () => import('@/views/salary/SalaryItemView.vue'),
          meta: { title: '薪资项目' },
        },
        {
          path: 'salary/standard',
          name: 'salaryStandard',
          component: () => import('@/views/salary/SalaryStandardView.vue'),
          meta: { title: '薪资标准' },
        },
        // 审批管理
        {
          path: 'approval/flow',
          name: 'approvalFlow',
          component: () => import('@/views/approval/FlowListView.vue'),
          meta: { title: '审批流程配置' },
        },
        {
          path: 'approval/pending',
          name: 'approvalPending',
          component: () => import('@/views/approval/PendingView.vue'),
          meta: { title: '待我审批' },
        },
        {
          path: 'approval/approved',
          name: 'approvalApproved',
          component: () => import('@/views/approval/ApprovedView.vue'),
          meta: { title: '已审批记录' },
        },
        // 报表统计
        {
          path: 'report/index',
          name: 'reportIndex',
          component: () => import('@/views/report/ReportView.vue'),
          meta: { title: '统计报表' },
        },
        // AI离职预测
        {
          path: 'ai/resignation-prediction',
          name: 'resignationPrediction',
          component: () => import('@/views/ai/ResignationPrediction.vue'),
          meta: { title: 'AI离职率预测' },
        },
        // 招聘管理
        {
          path: 'recruit/job',
          name: 'recruitJob',
          component: () => import('@/views/recruit/JobListView.vue'),
          meta: { title: '招聘岗位' },
        },
        {
          path: 'recruit/resume',
          name: 'recruitResume',
          component: () => import('@/views/recruit/ResumeListView.vue'),
          meta: { title: '简历管理' },
        },
        {
          path: 'recruit/interview',
          name: 'recruitInterview',
          component: () => import('@/views/recruit/InterviewListView.vue'),
          meta: { title: '面试管理' },
        },
        // 培训管理
        {
          path: 'train/list',
          name: 'trainList',
          component: () => import('@/views/train/TrainListView.vue'),
          meta: { title: '培训计划' },
        },
        {
          path: 'train/my',
          name: 'myTrain',
          component: () => import('@/views/train/MyTrainView.vue'),
          meta: { title: '我的培训' },
        },
        {
          path: 'train/request',
          name: 'trainRequest',
          component: () => import('@/views/train/MyTrainRequestView.vue'),
          meta: { title: '培训需求' },
        },
        {
          path: 'train/approval',
          name: 'trainApproval',
          component: () => import('@/views/train/TrainApprovalView.vue'),
          meta: { title: '培训审核' },
        },
        // 考核管理
        {
          path: 'assess/plan',
          name: 'assessPlan',
          component: () => import('@/views/assess/AssessPlanView.vue'),
          meta: { title: '考核方案' },
        },
        {
          path: 'assess/task',
          name: 'assessTask',
          component: () => import('@/views/assess/AssessTaskView.vue'),
          meta: { title: '考核任务管理' },
        },
        {
          path: 'assess/my',
          name: 'myAssess',
          component: () => import('@/views/assess/MyAssessView.vue'),
          meta: { title: '我的考核' },
        },
        {
          path: 'assess/result',
          name: 'assessResult',
          component: () => import('@/views/assess/AssessResultView.vue'),
          meta: { title: '考核结果' },
        },
        // 离职管理
        {
          path: 'resignation/apply',
          name: 'resignationApply',
          component: () => import('@/views/resignation/ResignationApplyView.vue'),
          meta: { title: '离职申请' },
        },
        {
          path: 'resignation/approve',
          name: 'resignationApprove',
          component: () => import('@/views/resignation/ResignationApproveView.vue'),
          meta: { title: '离职审批' },
        },
        // 个人中心
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/profile/ProfileView.vue'),
          meta: { title: '个人中心' },
        },
        {
          path: 'notification',
          name: 'notification',
          component: () => import('@/views/profile/NotificationView.vue'),
          meta: { title: '消息通知' },
        },
        // 系统管理
        {
          path: 'system/user',
          name: 'systemUser',
          component: () => import('@/views/system/UserView.vue'),
          meta: { title: '用户管理' },
        },
        {
          path: 'system/role',
          name: 'systemRole',
          component: () => import('@/views/system/RoleView.vue'),
          meta: { title: '角色管理' },
        },
        {
          path: 'system/menu',
          name: 'systemMenu',
          component: () => import('@/views/system/MenuView.vue'),
          meta: { title: '菜单管理' },
        },
        {
          path: 'system/dict',
          name: 'systemDict',
          component: () => import('@/views/system/DictView.vue'),
          meta: { title: '字典管理' },
        },
        {
          path: 'system/log',
          name: 'systemLog',
          component: () => import('@/views/system/OperationLogView.vue'),
          meta: { title: '操作日志' },
        },
        {
          path: 'system/holiday',
          name: 'systemHoliday',
          component: () => import('@/views/system/HolidayView.vue'),
          meta: { title: '节假日管理' },
        },
        {
          path: 'system/config',
          name: 'systemConfig',
          component: () => import('@/views/system/ConfigView.vue'),
          meta: { title: '系统配置' },
        },
        {
          path: 'system/approval-log',
          name: 'approvalLog',
          component: () => import('@/views/system/ApprovalLogView.vue'),
          meta: { title: '审批日志' },
        },
      ],
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.public) {
    next()
  } else if (!userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
