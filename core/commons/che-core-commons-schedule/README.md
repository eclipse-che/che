# Che job scheduling framework
## About
There is such a common programming use case when you need to execute some method periodically.
Usually it is implemented with some sort of a ThreadPoolExecutor. However, often developers don't pay
enough attention to thread start stop routine. As a result we have an unnamed thread or thread
that never stops, etc. Scheduling framework will take away all threading routine away from
developer and add a couple of new features.

## Features
- Run job with a fixed rate
- Run job with a fixed delay
- Run job according to the cron expression
- Error logging
- Container configuration
- Automatic job discovering
- Automatic thread pull start and shutdown.

## TODO
- Ability to run demon jobs (can be terminated during JVM shutdown)
- Metrics and statistic
- Ability to control thread names
- Time by UTC
- Do not interrupt future jobs on exceptions
- Ability to disable task.

## How to use
### Installation
There is a couple of steps you need to do before start. Usually you need to do it once, in target war.


First: add maven dependency.
```xml
<dependency>
    <groupId>org.eclipse.che.core</groupId>
    <artifactId>che-core-commons-schedule</artifactId>
</dependency>
```
Second: You need to install Guice module
```java
install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());
```
Thread: You need to configure core pool size. This is the minimum number of workers to keep alive.
```java
@Named("schedule.core_pool_size") Integer corePoolSize
```
Note: actual number of threads will be corePoolSize+1. One thread is needed to monitor cron jobs.

### Implementations notes
Framework can execute methods with any visibility and any name. But method must have 0 parameters.
If method that need to be executed is
```java
void run()
```
and class implements java.lang.Runnable then this method will be executed without reflection that suppose to be faster then using reflections. Best practices
is to implement Runnable interface and schedule method run.
Classes must be annotated with javax.inject.Singleton or com.google.inject.Singleton and binded in guice as Eager singleton.



### Run job with fixed rate
If you would like to execute some method with fixed rate. You can mark it with annotation
for execution a periodic action that becomes enabled first after the given initial delay,
and subsequently with the given period; that is executions will commence after initialDelay
then initialDelay+period, then initialDelay + 2 * period, and so on.
If any execution of the task encounters an exception, subsequent  executions are suppressed.
Otherwise, the task will only terminate via cancellation or termination of the executor.
If any execution of this task takes longer than its period,
then subsequent executions may start late, but will not concurrently execute.
Analogue of java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate

Example 1: Given method scheduleBackup will be executed once a minute after 1 minute initial delay.
```java

@Singleton
public class WorkspaceFsBackupScheduler {
...
   @ScheduleRate(initialDelay = 1, period = 1, unit = TimeUnit.MINUTES)
   public void scheduleBackup() {
   ...
   }
```

Example 2: Same as example 1, but timings configured over container named parameters.
```java
@Singleton
public class WorkspaceFsBackupScheduler {
    ...
    @ScheduleRate(initialDelayParameterName = "fs.backup.init_dalay",
                  periodParameterName = "fs.backup.period",
                  unit = TimeUnit.MINUTES)
    public void scheduleBackup() {
       ...
    }
```
<blockquote>
    <p>NOTE: if initialDelay and initialDelayParameterName  configured at the same time, initialDelayParameterName has greater weight
       when statically configured value. Same for period and periodParameterName.</p>
</blockquote>

### Run job with fixed delay
If you would like to execute some method with fixed delay you can mark method with annotation
for execution periodic action that becomes enabled first after the given initial delay, and subsequently
with the given delay between the termination of one execution and the commencement of the next.  Analogue of java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate

Example 1:  Given method registerRoutingRules will be executed with minute after 1 daley between end and start of
new job after 1 minute initial delay.
```java
@Singleton // should be eager
public class RouterRulesRegistry {

    @ScheduleDelay(initialDelay = 1,
            delay = 1,
            unit = TimeUnit.MINUTES)
    private void registerRoutingRules() throws Exception {
     ...
    }
```

Example 2:  Same as example 1, but timings configured over container named parameters.
```java
@Singleton // should be eager
public class RouterRulesRegistry {

    @ScheduleDelay(initialDelayParameterName = "router.rules.initialDelay",
            delayParameterName = "router.rules.delay",
            unit = TimeUnit.MINUTES)
    private void registerRoutingRules() throws Exception {
     ...
    }
```
<blockquote>
    <p>NOTE: if initialDelayParameterName and initialDelay  configured at the same time, initialDelayParameterName has greater weight
       when statically configured value. Same for delay and delayParameterName.</p>
</blockquote>

### Disabling scheduling by rate or period
If a scheduled method has been configured through `delayParameterName` or `periodParameterName`, it is possible to disable
the scheduling by setting a non positive value to the parameter.

Example: Disabling scheduling of this `registerRoutingRules` method:

```java
@Singleton // should be eager
public class RouterRulesRegistry {

    @ScheduleDelay(initialDelayParameterName = "router.rules.initialDelay",
            delayParameterName = "router.rules.delay",
            unit = TimeUnit.MINUTES)
    private void registerRoutingRules() throws Exception {
     ...
    }
```

In che.properties:
```
router.rules.delay=-1
```
will disable the scheduling

### Run job according to the cron expression
If you would like to execute some method according to the cron expression you can mark method with annotation @ScheduleCron
Example 1 :  Send each Sunday at 1:00 AM.

```java
@Singleton
public class ReportSender {
    @ScheduleCron(cron = "0 0 1 ? * SUN *")  //
    public void sendWeeklyReports() {
    ...
    }
```

Example 2 :   Same as example 1, but cron expression configured over container named parameter "report.sender.cron".

```java
@Singleton
public class ReportSender {
    @ScheduleCron(cronParameterName = "report.sender.cron")  //
    public void sendWeeklyReports() {
    ...
    }
```

<blockquote>
    <p>NOTE: if cronParameterName and cron  configured at the same time, cronParameterName has grater weight
       when statically configured value.</p>
</blockquote>

#### Cron expression syntax.
<dl>
Cron expressions provide the ability to specify complex time combinations such as 
&quot;At 8:00am every Monday through Friday&quot; or &quot;At 1:30am every
last Friday of the month&quot;.
<P>
Cron expressions are comprised of 6 required fields and one optional field
separated by white space. The fields respectively are described as follows:
  *
<table cellspacing="8">
<tr>
<th align="left">Field Name</th>
<th align="left">&nbsp;</th>
<th align="left">Allowed Values</th>
<th align="left">&nbsp;</th>
<th align="left">Allowed Special Characters</th>
</tr>
<tr>
<td align="left"><code>Seconds</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>0-59</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>, - * /</code></td>
</tr>
<tr>
<td align="left"><code>Minutes</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>0-59</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>, - * /</code></td>
</tr>
<tr>
<td align="left"><code>Hours</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>0-23</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>, - * /</code></td>
</tr>
<tr>
<td align="left"><code>Day-of-month</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>1-31</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>, - * ? / L W</code></td>
</tr>
<tr>
<td align="left"><code>Month</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>1-12 or JAN-DEC</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>, - * /</code></td>
</tr>
<tr>
<td align="left"><code>Day-of-Week</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>1-7 or SUN-SAT</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>, - * ? / L #</code></td>
</tr>
<tr>
<td align="left"><code>Year (Optional)</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>empty, 1970-2199</code></td>
<td align="left">&nbsp;</th>
<td align="left"><code>, - * /</code></td>
</tr>
</table>
<P>
The '*' character is used to specify all values. For example, &quot;*&quot;
in the minute field means &quot;every minute&quot;.
<P>
The '?' character is allowed for the day-of-month and day-of-week fields. It
is used to specify 'no specific value'. This is useful when you need to
specify something in one of the two fields, but not the other.
<P>
The '-' character is used to specify ranges For example &quot;10-12&quot; in
the hour field means &quot;the hours 10, 11 and 12&quot;.
<P>
The ',' character is used to specify additional values. For example
&quot;MON,WED,FRI&quot; in the day-of-week field means &quot;the days Monday,
Wednesday, and Friday&quot;.
<P>
The '/' character is used to specify increments. For example &quot;0/15&quot;
in the seconds field means &quot;the seconds 0, 15, 30, and 45&quot;. And
&quot;5/15&quot; in the seconds field means &quot;the seconds 5, 20, 35, and
50&quot;.  Specifying '*' before the  '/' is equivalent to specifying 0 is
the value to start with. Essentially, for each field in the expression, there
is a set of numbers that can be turned on or off. For seconds and minutes,
the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to
31, and for months 1 to 12. The &quot;/&quot; character simply helps you turn
on every &quot;nth&quot; value in the given set. Thus &quot;7/6&quot; in the
month field only turns on month &quot;7&quot;, it does NOT mean every 6th
month, please note that subtlety.
<P>
The 'L' character is allowed for the day-of-month and day-of-week fields.
This character is short-hand for &quot;last&quot;, but it has different
meaning in each of the two fields. For example, the value &quot;L&quot; in
the day-of-month field means &quot;the last day of the month&quot; - day 31
for January, day 28 for February on non-leap years. If used in the
day-of-week field by itself, it simply means &quot;7&quot; or
&quot;SAT&quot;. But if used in the day-of-week field after another value, it
means &quot;the last xxx day of the month&quot; - for example &quot;6L&quot;
means &quot;the last friday of the month&quot;. You can also specify an offset
from the last day of the month, such as "L-3" which would mean the third-to-last
day of the calendar month. <i>When using the 'L' option, it is important not to
specify lists, or ranges of values, as you'll get confusing/unexpected results.</i>
<P>
The 'W' character is allowed for the day-of-month field.  This character
is used to specify the weekday (Monday-Friday) nearest the given day.  As an
example, if you were to specify &quot;15W&quot; as the value for the
day-of-month field, the meaning is: &quot;the nearest weekday to the 15th of
the month&quot;. So if the 15th is a Saturday, the trigger will fire on
Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the
16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th.
However if you specify &quot;1W&quot; as the value for day-of-month, and the
1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not
'jump' over the boundary of a month's days.  The 'W' character can only be
specified when the day-of-month is a single day, not a range or list of days.
<P>
The 'L' and 'W' characters can also be combined for the day-of-month
expression to yield 'LW', which translates to &quot;last weekday of the
month&quot;.
<P>
The '#' character is allowed for the day-of-week field. This character is
used to specify &quot;the nth&quot; XXX day of the month. For example, the
value of &quot;6#3&quot; in the day-of-week field means the third Friday of
the month (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month).
Other examples: &quot;2#1&quot; = the first Monday of the month and
&quot;4#5&quot; = the fifth Wednesday of the month. Note that if you specify
&quot;#5&quot; and there is not 5 of the given day-of-week in the month, then
no firing will occur that month.  If the '#' character is used, there can
only be one expression in the day-of-week field (&quot;3#1,6#3&quot; is
not valid, since there are two expressions).
<P>
<!--The 'C' character is allowed for the day-of-month and day-of-week fields.
This character is short-hand for "calendar". This means values are
calculated against the associated calendar, if any. If no calendar is
associated, then it is equivalent to having an all-inclusive calendar. A
value of "5C" in the day-of-month field means "the first day included by the
calendar on or after the 5th". A value of "1C" in the day-of-week field
means "the first day included by the calendar on or after Sunday".-->
<P>
The legal characters and the names of months and days of the week are not
case sensitive.
  *
<p>
<b>NOTES:</b>
<ul>
<li>Support for specifying both a day-of-week and a day-of-month value is
not complete (you'll need to use the '?' character in one of these fields).
</li>
<li>Overflowing ranges is supported - that is, having a larger number on
the left hand side than the right. You might do 22-2 to catch 10 o'clock
at night until 2 o'clock in the morning, or you might have NOV-FEB. It is
very important to note that overuse of overflowing ranges creates ranges
that don't make sense and no effort has been made to determine which
interpretation CronExpression chooses. An example would be
"0 0 14-6 ? * FRI-MON". </li>
</ul>
</p>
<dl>
