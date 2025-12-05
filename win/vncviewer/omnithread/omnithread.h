#ifndef __omnithread_h_
#define __omnithread_h_

#if defined(_MSC_VER) && _MSC_VER < 1900
#define noexcept(a)
#endif

#ifndef NULL
#define NULL (void *)0
#endif

class omni_mutex;
class omni_condition;
class omni_semaphore;
class omni_thread;

#ifndef OMNI_THREAD_EXPOSE
#define OMNI_THREAD_EXPOSE private
#endif

#if defined(__arm__) && defined(__atmos__)
#include <omnithread/posix.h>

#elif defined(__alpha__) && defined(__osf1__)
#include <omnithread/posix.h>

#elif defined(__powerpc__) && defined(__aix__)
#include <omnithread/posix.h>

#elif defined(__hpux__)
#include <omnithread/posix.h>

#elif defined(__WIN32__)
#include "nt.h"

#ifdef _MSC_VER

#if defined(_OMNITHREAD_DLL) && defined(_WINSTATIC)
#error "Both _OMNITHREAD_DLL and _WINSTATIC are defined."
#elif defined(_OMNITHREAD_DLL)
#define _OMNITHREAD_NTDLL_ __declspec(dllexport)
#elif !defined(_WINSTATIC)
#define _OMNITHREAD_NTDLL_ __declspec(dllimport)
#elif defined(_WINSTATIC)
#define _OMNITHREAD_NTDLL_
#endif

#else

#define _OMNITHREAD_NTDLL_

#endif

#elif defined(__sunos__) && (__OSVERSION__ == 5)
#ifdef UsePthread
#include <omnithread/posix.h>
#else
#include <omnithread/solaris.h>
#endif

#elif defined(__linux__)
#include <omnithread/posix.h>

#elif defined(__nextstep__)
#include <omnithread/mach.h>

#elif defined(__VMS)
#include <omnithread/posix.h>

#elif defined(__SINIX__)
#include <omnithread/posix.h>

#elif defined(__osr5__)
#include <omnithread/posix.h>

#elif defined(__irix__)
#include <omnithread/posix.h>

#else
#error "No implementation header file"
#endif

#if !defined(__WIN32__)
#define _OMNITHREAD_NTDLL_
#endif

#if (!defined(OMNI_MUTEX_IMPLEMENTATION) ||  \
     !defined(OMNI_CONDITION_IMPLEMENTATION) ||  \
     !defined(OMNI_SEMAPHORE_IMPLEMENTATION) ||  \
     !defined(OMNI_THREAD_IMPLEMENTATION))
#error "Implementation header file incomplete"
#endif

class _OMNITHREAD_NTDLL_ omni_thread_fatal
{
  public:
    int error;
    omni_thread_fatal(int e = 0) : error(e) {}
};

class _OMNITHREAD_NTDLL_ omni_thread_invalid {};

class _OMNITHREAD_NTDLL_ omni_mutex
{
  public:
    omni_mutex(void);
    ~omni_mutex(void);

    void lock(void);
    void unlock(void);
    void acquire(void) { lock(); }
    void release(void) { unlock(); }

    friend class omni_condition;

  private:

    omni_mutex(const omni_mutex &);
    omni_mutex &operator = (const omni_mutex &);

  OMNI_THREAD_EXPOSE:
    OMNI_MUTEX_IMPLEMENTATION
};

class _OMNITHREAD_NTDLL_ omni_mutex_lock
{
  omni_mutex &mutex;

  public:
    omni_mutex_lock(omni_mutex &m) : mutex(m) { mutex.lock(); }
    ~omni_mutex_lock(void) { mutex.unlock(); }

  private:

    omni_mutex_lock(const omni_mutex_lock &);
    omni_mutex_lock &operator = (const omni_mutex_lock &);
};

class _OMNITHREAD_NTDLL_ omni_condition
{
  omni_mutex *mutex;

  public:
    omni_condition(omni_mutex *m);

    ~omni_condition(void);

    void wait(void);

    int timedwait(unsigned long secs, unsigned long nanosecs = 0);

    void signal(void);

    void broadcast(void);

  private:

    omni_condition(const omni_condition &);
    omni_condition &operator = (const omni_condition &);

  OMNI_THREAD_EXPOSE:
    OMNI_CONDITION_IMPLEMENTATION
};

class _OMNITHREAD_NTDLL_ omni_semaphore
{
  public:
    omni_semaphore(unsigned int initial = 1);
    ~omni_semaphore(void) noexcept(false);

    void wait(void);

    int trywait(void);

    void post(void);

  private:

    omni_semaphore(const omni_semaphore &);
    omni_semaphore &operator = (const omni_semaphore &);

  OMNI_THREAD_EXPOSE:
    OMNI_SEMAPHORE_IMPLEMENTATION
};

class _OMNITHREAD_NTDLL_ omni_semaphore_lock
{
  omni_semaphore &sem;

  public:
    omni_semaphore_lock(omni_semaphore &s) : sem(s) { sem.wait(); }
    ~omni_semaphore_lock(void) { sem.post(); }

  private:

    omni_semaphore_lock(const omni_semaphore_lock &);
    omni_semaphore_lock &operator = (const omni_semaphore_lock &);
};

class _OMNITHREAD_NTDLL_ omni_thread
{
  public:

    enum priority_t {
      PRIORITY_LOW,
      PRIORITY_NORMAL,
      PRIORITY_HIGH
    };

    enum state_t {
      STATE_NEW,

      STATE_RUNNING,
      STATE_TERMINATED

    };

    omni_thread(void (*fn) (void *), void *arg = NULL,
                priority_t pri = PRIORITY_NORMAL);
    omni_thread(void *(*fn) (void *), void *arg = NULL,
                priority_t pri = PRIORITY_NORMAL);

    void start(void);

  protected:

    omni_thread(void *arg = NULL, priority_t pri = PRIORITY_NORMAL);

    void start_undetached(void);

    virtual ~omni_thread(void) noexcept(false);

  public:

    void join(void **);

    void set_priority(priority_t);

    static omni_thread *create(void (*fn) (void *), void *arg = NULL,
                               priority_t pri = PRIORITY_NORMAL);
    static omni_thread *create(void *(*fn) (void *), void *arg = NULL,
                               priority_t pri = PRIORITY_NORMAL);

    static void exit(void *return_value = NULL);

    static omni_thread *self(void);

    static void yield(void);

    static void sleep(unsigned long secs, unsigned long nanosecs = 0);

    static void get_time(unsigned long *abs_sec, unsigned long *abs_nsec,
                         unsigned long rel_sec = 0,
                         unsigned long rel_nsec = 0);

  private:

    virtual void run(void *arg) {}
    virtual void *run_undetached(void *arg) { return NULL; }

    void common_constructor(void *arg, priority_t pri, int det);

    omni_mutex mutex;

    state_t _state;
    priority_t _priority;

    static omni_mutex *next_id_mutex;
    static int next_id;
    int _id;

    void (*fn_void) (void *);
    void *(*fn_ret) (void *);
    void *thread_arg;
    int detached;

  public:

    priority_t priority(void)
    {

      omni_mutex_lock l(mutex);
      return _priority;
    }

    state_t state(void)
    {

      omni_mutex_lock l(mutex);
      return _state;
    }

    int id(void) { return _id; }

    class _OMNITHREAD_NTDLL_ init_t
    {
      static int count;

      public:
        init_t(void);
    };

    friend class init_t;

  OMNI_THREAD_EXPOSE:
    OMNI_THREAD_IMPLEMENTATION
};

static omni_thread::init_t omni_thread_init;

#endif
